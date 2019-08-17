package com.myself.spider.plantform;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

/**
 * @Auther: Holeski
 * @Date: 2019/6/21 09:09
 * @Description:
 */
@Component
public interface PictureRepository extends JpaRepository<Picture,Integer>, JpaSpecificationExecutor<Picture>, Serializable {

    List<Picture> findAllByCreateDate(String createDate);
}
