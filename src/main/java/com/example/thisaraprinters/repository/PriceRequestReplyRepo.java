package com.example.thisaraprinters.repository;

import com.example.thisaraprinters.model.PriceRequestReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceRequestReplyRepo extends JpaRepository<PriceRequestReply, Integer> {
    List<PriceRequestReply> findByPriceRequestId(Integer priceRequestId);
}
