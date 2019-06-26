package com.myself.spider;

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
    private PictureMapper dao;

    public boolean insert(Picture picture) {
        return dao.insert(picture) > 0;
    }

    public Picture selectById(int id) {
        return dao.selectById(id);
    }

    public List<Picture> selectAll() {
        return dao.selectAll();
    }

    public boolean updateValue(Picture picture) {
        return dao.updateValue(picture) > 0;
    }

    public boolean delete(Integer id) {
        return dao.delete(id) > 0;
    }
}
