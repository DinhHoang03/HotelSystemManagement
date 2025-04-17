package com.humg.HotelSystemManagement.service.paymentService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.humg.HotelSystemManagement.crypto.HMACUtil;
import com.humg.HotelSystemManagement.configuration.payment.ZaloPayConfig;
import com.humg.HotelSystemManagement.dto.request.payment.ZaloPayOrderRequest;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.booking.BookingBillRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ZaloPayService {
    ZaloPayConfig zaloPayConfig;
    BookingBillRepository bookingBillRepository;

    public static String getCurrentTimeString(String format){
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        fmt.setCalendar(cal);
        return fmt.format(cal.getTimeInMillis());
    }

    public String createPayment(ZaloPayOrderRequest request) throws IOException {
        if(request == null) {
            throw new AppException(AppErrorCode.REQUEST_IS_NULL);
        }

        Random random = new Random();
        int randomId = random.nextInt(1000000);

        var bookingBill = bookingBillRepository.findById(request.getBookingBillId())
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        Map<String, Object> zaloPayOrder = new HashMap<>();
        zaloPayOrder.put("app_id", 553);
        zaloPayOrder.put("app_trans_id", getCurrentTimeString("yyMMdd") + "_" + randomId);
        zaloPayOrder.put("app_time", System.currentTimeMillis());
        zaloPayOrder.put("app_user", bookingBill.getBooking().getCustomer().getUsername());
        zaloPayOrder.put("amount", bookingBill.getGrandTotal());
        zaloPayOrder.put("description", "DinhRiseHotel - Payment for the order #" + request.getBookingBillId());
        zaloPayOrder.put("bankcode", "");

// Khởi tạo ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

// Tạo JSON cho item
        Map<String, Object> itemMap = new LinkedHashMap<>();
        itemMap.put("itemid", bookingBill.getBookingBillId());
        itemMap.put("itename", "booking");
        itemMap.put("itemprice", bookingBill.getGrandTotal());
        itemMap.put("itemquantity", 1);

        List<Map<String, Object>> itemList = new ArrayList<>();
        itemList.add(itemMap);

// Convert itemList thành JSON string
        String itemJsonString = objectMapper.writeValueAsString(itemList);

// Tạo embed_data
        Map<String, Object> embedData = new LinkedHashMap<>();
        embedData.put("promotioninfo", "");
        embedData.put("merchantinfo", "hotel");
        //embedData.put("redirecturl", zaloPayConfig.getRedirectUrl());

        /**
        Map<String, String> columnInfo = new LinkedHashMap<>();
        columnInfo.put("storename", "hotel");
        embedData.put("columninfo", columnInfo);

         */
// Convert embedData thành JSON string
        String embedDataJsonString = objectMapper.writeValueAsString(embedData);

// Put vào zaloPayOrder
        zaloPayOrder.put("item", itemJsonString);
        zaloPayOrder.put("embed_data", embedDataJsonString);
        zaloPayOrder.put("callback_url", zaloPayConfig.getCallbackUrl());

// Tạo chuỗi data để sinh MAC
        String data = zaloPayOrder.get("app_id") + "|" + zaloPayOrder.get("app_trans_id") + "|"
                + zaloPayOrder.get("app_user") + "|" + zaloPayOrder.get("amount") + "|"
                + zaloPayOrder.get("app_time") + "|" + embedDataJsonString + "|" + itemJsonString;

        log.info("Data for MAC: {}", data);

// Tiếp theo: sinh MAC, gửi request như cũ
        String mac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, "9phuAOYhan4urywHTh0ndEXiV3pKHr5Q", data);

        //String mac = HMACUtil.computeHMac(data, ZaloPayConfig.KEY1);
        log.info("Generated MAC: {}", mac);
        zaloPayOrder.put("mac", mac);

        try (CloseableHttpClient client = HttpClients.createDefault()){
            HttpPost post = new HttpPost(zaloPayConfig.getCreateOrderUrl());
            List<NameValuePair> params = new ArrayList<>();
            for(Map.Entry<String, Object> e : zaloPayOrder.entrySet()) {
                params.add(new BasicNameValuePair(e.getKey(), e.getValue().toString()));
            }
            post.setEntity(new UrlEncodedFormEntity(params));
            log.info("Sending request to ZaloPay: {}", params);

            try (CloseableHttpResponse response = client.execute(post)) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuilder resultJsonString = new StringBuilder();
                String line;while((line = reader.readLine()) != null) {
                    resultJsonString.append(line);
                }
                System.out.println("Zalopay Response: " + resultJsonString.toString());

                return resultJsonString.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to create order: " + e.getMessage() + "\"}";
        }
    }

    public String getOrderStatus(String appTransId) {
        String data = zaloPayConfig.getAppId() + "|" + appTransId + "|" + zaloPayConfig.getKey1();
        String mac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, zaloPayConfig.getKey1(), data);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("https://sb-openapi.zalopay.vn/v2/query");

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("app_id", zaloPayConfig.getAppId().toString()));
            params.add(new BasicNameValuePair("app_trans_id", appTransId));
            params.add(new BasicNameValuePair("mac", mac));

            post.setEntity(new UrlEncodedFormEntity(params));

            try (CloseableHttpResponse response = client.execute(post)) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuilder resultJsonStr = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    resultJsonStr.append(line);
                }

                return resultJsonStr.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to get order status: " + e.getMessage() + "\"}";
        }
    }
}
