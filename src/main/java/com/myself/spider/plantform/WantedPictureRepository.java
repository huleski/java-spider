package com.myself.spider.plantform;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

/**
 * @Auther: Holeski
 * @Date: 2020/4/21 09:09
 * @Description:
 */
@Component
public interface WantedPictureRepository extends JpaRepository<WantedPicture,Integer>, JpaSpecificationExecutor<WantedPicture>, Serializable {
    List<WantedPicture> findAllBySearchKey(String SearchKey);

    List<WantedPicture> findAllByCreateDate(String createDate);
}
