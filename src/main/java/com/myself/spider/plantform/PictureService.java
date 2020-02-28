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
    private PictureRepository dao;

    public void saveAndUpdate(Picture picture) {
        dao.save(picture);
    }

    public Picture selectById(int id) {
        return dao.getOne(id);
    }

    public List<Picture> selectAll() {
        return dao.findAll();
    }

    public void delete(Integer id) {
        dao.deleteById(id);
    }

    public List<Picture> saveAll(List<Picture> pics) {
        List list = dao.saveAll(pics);
        return list;
    }

    public List<Picture> saveAllUnsaved(List<Picture> pics) {
        List<Picture> collect = pics.stream().filter(picture -> {
            List<Picture> results = dao.findAllByIllustIdAndSort(picture.getIllustId(), picture.getSort());
            return results == null || results.size() < 1;
        }).collect(Collectors.toList());
        List list = dao.saveAll(collect);
        log.info("保存了" + list.size() + "张图片");
        return collect;
    }

    public List<Picture> findAllByCreateDate(String createDate) {
        return dao.findAllByCreateDate(createDate);
    }
}
