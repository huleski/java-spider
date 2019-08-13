package com.myself.spider.WxPlantform;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Auther: Holeski
 * @Date: 2019/6/21 09:13
 * @Description:
 */
@Service
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

    public boolean saveAll(List<Picture> pics) {
        List list = dao.saveAll(pics);
        return list.size() > 0;
    }

    public List<Picture> selectToday(String createDate) {
        return dao.findAllByCreateDate(createDate);
    }
}
