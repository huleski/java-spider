package com.myself.spider;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @Auther: Holeski
 * @Date: 2019/6/21 09:09
 * @Description:
 */
@Component
public interface PictureDao extends JpaRepository<Picture,Integer>, JpaSpecificationExecutor<Picture>, Serializable {

}
