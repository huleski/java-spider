package com.myself.spider.plantform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
        pics = wantedPictureDao.saveAll(pics);
        log.info("保存了" + pics.size() + "张图片");
        return pics;
    }

    public List findAllByCreateDate(String createDate) {
        return wantedPictureDao.findAllByCreateDate(createDate);
    }

    public List findAllBySearchKey(String searchKey) {
        return wantedPictureDao.findAllBySearchKeyOrderByUser(searchKey);
    }

    public List saveAllUnsaved(List<WantedPicture> list) {
        List<WantedPicture> collect = list.stream().filter(picture -> {
            List<WantedPicture> results = wantedPictureDao.findAllByIllustIdAndSortAndSearchKey(picture.getIllustId(), picture.getSort(), picture.getSearchKey());
            return results == null || results.size() < 1;
        }).collect(Collectors.toList());
        list = wantedPictureDao.saveAll(collect);
        log.info("保存了" + list.size() + "张图片");
        return list;
    }
}
