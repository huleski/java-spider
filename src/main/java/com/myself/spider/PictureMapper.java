package com.myself.spider;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Auther: Holeski
 * @Date: 2019/6/21 09:09
 * @Description:
 */
@Mapper
@Component
public interface PictureMapper {
    /** 插入 并查询id 赋给传入的对象 */
    @Insert("INSERT INTO picture(user, user_id, user_avator, title, illust_id, original_img, later_img, sort, create_time) " +
            "VALUES(#{user}, #{userId}, #{userAvator}, #{title}, #{illustId}, #{originalImg}, #{laterImg}, #{sort}, #{createTime})")
    @SelectKey(statement = "SELECT seq id FROM sqlite_sequence WHERE (name = 'picture')", before = false, keyProperty = "id", resultType = int.class)
    int insert(Picture model);

    /** 根据 ID 查询 */
    @Select("SELECT * FROM picture WHERE id=#{id}")
    Picture selectById(int id);


    /** 根据 illust_id 查询 */
    @Select("SELECT * FROM picture WHERE illust_id=#{illustId}")
    Picture selectByIllustId(int illustId);

    /** 查询全部 */
    @Select("SELECT * FROM picture")
    List<Picture> selectAll();

    /** 更新 value */
    @Update("UPDATE picture SET value=#{user} WHERE user=#{user}")
    int updateValue(Picture model);

    /** 根据 ID 删除 */
    @Delete("DELETE FROM picture WHERE id=#{id}")
    int delete(Integer id);
}
