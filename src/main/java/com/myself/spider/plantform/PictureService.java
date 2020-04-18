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
public class PictureService {
    @Autowired
    private PictureRepository pictureDao;
    @Autowired
    private WantedPictureRepository wantedPictureDao;

    public void saveAndUpdate(Picture picture) {
        pictureDao.save(picture);
    }

    public Picture selectById(int id) {
        return pictureDao.getOne(id);
    }

    public List<Picture> selectAll() {
        return pictureDao.findAll();
    }

    public void delete(Integer id) {
        pictureDao.deleteById(id);
    }

    public List<Picture> saveAll(List<Picture> pics) {
        return pictureDao.saveAll(pics);
    }

    public List saveAllUnsaved(List<Picture> pics) {
        List<Picture> collect = pics.stream().filter(picture -> {
            List<Picture> results = pictureDao.findAllByIllustIdAndSort(picture.getIllustId(), picture.getSort());
            return results == null || results.size() < 1;
        }).collect(Collectors.toList());
        List list = pictureDao.saveAll(collect);
        log.info("保存了" + list.size() + "张图片");
        return collect;
    }

    public List findAllByCreateDate(String createDate) {
        return pictureDao.findAllByCreateDate(createDate);
    }
}
