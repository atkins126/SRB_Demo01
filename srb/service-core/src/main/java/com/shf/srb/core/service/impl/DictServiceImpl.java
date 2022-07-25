package com.shf.srb.core.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shf.srb.core.listener.ExcelEditDTOListener;
import com.shf.srb.core.pojo.dto.ExcelDictDTO;
import com.shf.srb.core.pojo.entity.Dict;
import com.shf.srb.core.mapper.DictMapper;
import com.shf.srb.core.service.DictService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 数据字典 服务实现类
 * </p>
 *
 * @author shuhongfan
 * @since 2022-07-23
 */
@Service
@Slf4j
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    /**
     * Excel数据的导入
     * @param inputStream
     */
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void importData(InputStream inputStream) {
        EasyExcel.read(
                        inputStream,
                        ExcelDictDTO.class,
                        new ExcelEditDTOListener(baseMapper)
                )
                .sheet()
                .doRead();

        log.info("Excel导入成功");
    }

    /**
     * Excel数据的导出
     * @return
     */
    @Override
    public List listDictData() {
        List<Dict> dictList = baseMapper.selectList(null);

//        创建ExcelDictDTO对象，将Dict列表转换成ExcelDictDTO对象
        ArrayList<ExcelDictDTO> excelDictDTOList = new ArrayList<>(dictList.size());
        dictList.forEach(dict->{
            ExcelDictDTO excelDictDTO = new ExcelDictDTO();
            BeanUtils.copyProperties(dict, excelDictDTO);
            excelDictDTOList.add(excelDictDTO);
        });
        return excelDictDTOList;
    }

    /**
     * 根据上级id获取子节点数据列表
     * @param parentId
     * @return
     */
    @Override
    public List<Dict> listByParentId(Long parentId) {
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("parent_id", parentId);
        List<Dict> dictList = baseMapper.selectList(dictQueryWrapper);
//        填充hasChildren字段
        dictList.forEach(dict->{
//            判断当前节点是否有子节点，找到当前dict下级没有子节点
            boolean hasChildren = hasChildren(dict.getId());
            dict.setHasChildren(hasChildren);
        });
        return dictList;
    }

    /**
     * 判断当前id所在的节点是否有子节点
     * @param id
     * @return
     */
    private boolean hasChildren(Long id) {
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("parent_id", id);
        Integer count = baseMapper.selectCount(dictQueryWrapper);
        if(count.intValue() > 0) {
            return true;
        }
        return false;
    }
}
