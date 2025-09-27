package org.pierce.list.imp;

import org.apache.ibatis.session.SqlSession;
import org.pierce.DataBase;
import org.pierce.UtilTools;
import org.pierce.list.Directive;
import org.pierce.list.MatchType;
import org.pierce.list.NameListCheck;
import org.pierce.list.entity.EntityDesc;
import org.pierce.list.entity.NameEntity;
import org.pierce.list.mapper.NameListMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataBaseNameListCheck extends DefaultNameListCheck implements NameListCheck {

    final Pattern pattern = Pattern.compile("(.*)/(.*)");

    List<EntityDesc> entityDescList = new ArrayList<>();

    private static final Logger log = LoggerFactory.getLogger(GFWNameListCheck.class);

    private static final DataBaseNameListCheck instance = new DataBaseNameListCheck();

    public static DataBaseNameListCheck getInstance() {
        return instance;
    }

    public String list() {
        return UtilTools.objToString(entityDescList.reversed(),true);
    }

    public void reload() {
        log.info("reload");
        load();

    }

    public void load() {
        final List<EntityDesc> entityDescList = new ArrayList<>();
        try (SqlSession sqlSession = DataBase.getSqlSessionFactory().openSession()) {
            NameListMapper mapper = sqlSession.getMapper(NameListMapper.class);
            List<NameEntity> list = mapper.selectAll();

            for (NameEntity nameEntity : list) {
                Directive directive = Directive.valueOf(nameEntity.getDirective());
                MatchType matchType = MatchType.valueOf(nameEntity.getMatchType());

                EntityDesc entityDesc = new EntityDesc();
                entityDesc.setDirective(directive);
                entityDesc.setMatchType(matchType);


                String data = nameEntity.getData();
                entityDesc.setData(data);
                if (matchType == MatchType.REGULAR_MATCHING) {
                    entityDesc.setPattern(Pattern.compile(data));
                } else if (matchType == MatchType.SUBNET) {
                    Matcher m1 = pattern.matcher(data);
                    if (m1.find()) {
                        String address = m1.group(1);
                        int cidrLen = Integer.parseInt(m1.group(2));

                        entityDesc.setAddress(address);
                        entityDesc.setCidrLen(cidrLen);
                    }
                }
                entityDescList.add(entityDesc);
            }
            this.entityDescList = entityDescList;

        }
    }

    private DataBaseNameListCheck() {
        load();
    }

    @Override
    public Directive check(String address, int port) {

        for (EntityDesc entityDesc : entityDescList) {
            Directive directive = entityDesc.test(address);
            if (directive != Directive.MISS) {
                return directive;
            }
        }
        return super.check(address, port);
    }

}
