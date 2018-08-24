package org.zxp.jobexcutor.service.impl;

import org.zxp.jobexcutor.entity.PrpCMainDemo;
import org.zxp.jobexcutor.mapper.PrpCMainDemoMapper;
import org.zxp.jobexcutor.service.DemoJobService;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service("demoJobService")
@Transactional
public class DemoJobServiceImpl implements DemoJobService {
    @Autowired
    PrpCMainDemoMapper mapper;
    @Resource(name = "sqlSessionTemplate")
    private SqlSessionTemplate sqlSessionTemplate;

    public SqlSessionTemplate getSqlSessionTemplate()
    {
        return sqlSessionTemplate;
    }

    @Override
    public void saveAll(List<PrpCMainDemo> list) throws Exception {
//        mapper.insertAll(list);
        //换成如下方式
        PrpCMainDemo prpCMainDemo;
        SqlSession sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);//跟上述sql区别
        for (int i = 0; i < list.size(); i++) {
            mapper.insert(list.get(i));
        }
        sqlSession.commit();
    }
}
