package com.humg.HotelSystemManagement.entity.totalServices;

import com.humg.HotelSystemManagement.entity.booking.BookingItems;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "hotel_offers")
public class HotelOffers {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "hotel_offer_id")
    String hotelServiceId;

    /**
    //@Column(name = "service_type", nullable = false)
    //@Enumerated(EnumType.STRING)
    ServiceTypes serviceTypes;
    **/

    @Column(name = "service_type", nullable = false)
    String serviceTypes;

    @Column(name = "price", nullable = false)
    Long price;

    @OneToMany(mappedBy = "hotelOffers",cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<BookingItems> bookingItems = new ArrayList<>();

    /**
    //Dòng code này sẽ tự động gán giá khi lưu vào database
     //Một cách viết thủ công nhất
    public void setDefaultPrice() {
        if(price == null){
            switch(serviceTypes){
                case LAUNDRY:
                    price = 100000L;
                    break;
                case BREAKFAST:
                    price = 80000L;
                    break;
                case SPA_AND_MASSAGE:
                    price = 300000L;
                    break;
                case MINIBAR:
                    price = 150000L;
                    break;
                case LATE_CHECK_OUT:
                    price = 200000L;
                    break;
            }
        }
    }
     **/

    /**
    @PrePersist
    public void setDefaultPrice(){
        if(price == null){
            this.price = switch (serviceTypes){
                case ServiceTypes.LAUNDRY -> 100000l;
                case ServiceTypes.BREAKFAST ->  80000L;
                case ServiceTypes.SPA_AND_MASSAGE -> 300000L;
                case ServiceTypes.MINIBAR -> 150000L;
                case ServiceTypes.LATE_CHECK_OUT -> 200000L;
            };
        }
    }
    **/
}
