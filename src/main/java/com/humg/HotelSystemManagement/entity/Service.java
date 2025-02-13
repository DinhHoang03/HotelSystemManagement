package com.humg.HotelSystemManagement.entity;

import com.humg.HotelSystemManagement.entity.enums.ServiceTypes;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    Long serviceId;

    @Column(name = "service_type", nullable = false)
    @Enumerated(EnumType.STRING)
    ServiceTypes serviceTypes;

    @Column(name = "price", nullable = false)
    Long price;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    List<BookingService> bookingServices;

    //Dòng code này sẽ tự động gán giá khi lưu vào database
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
}
