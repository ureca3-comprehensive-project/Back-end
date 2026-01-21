package org.backend.domain.invoice.entity;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "invoiceDetails",
        indexes = {
        })
public class InvoiceDetail {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long invoiceDetailId;

	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "invoice_id")
	    private Invoice invoice;

	    private String billingType;
	    private String status;
	    private BigDecimal amount;

}
