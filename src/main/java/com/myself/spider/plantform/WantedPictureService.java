package com.myself.spider.plantform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Auther: Holeski
 * @Date: 2019/6/21 09:13
 * @Description:
 */
@Service
@Slf4j
public class WantedPictureService {
    @Autowired
    private WantedPictureRepository wantedPictureDao;

    public List<WantedPicture> saveAll(List<WantedPicture> pics) {
        return wantedPictureDao.saveAll(pics);
    }

    public List findAllByCreateDate(String createDate) {
        return wantedPictureDao.findAllByCreateDate(createDate);
    }

    public List findAllBySearchKey(String searchKey) {
        return wantedPictureDao.findAllBySearchKey(searchKey);
    }
}
