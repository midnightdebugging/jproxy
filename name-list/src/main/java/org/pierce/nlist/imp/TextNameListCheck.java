package org.pierce.nlist.imp;

import org.pierce.nlist.Directive;
import org.pierce.nlist.MatchType;
import org.pierce.nlist.NameListCheck;
import org.pierce.nlist.entity.EntityDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextNameListCheck implements NameListCheck {
    private static final Logger log = LoggerFactory.getLogger(TextNameListCheck.class);


    protected Pattern p0 = Pattern.compile("([^ ]+) +([^ ]+) +([^ ]+)");
    protected Pattern p1 = Pattern.compile("(.*)/(.*)");
    List<EntityDesc> entityDescList = new ArrayList<>();

    public TextNameListCheck() {

    }

    public TextNameListCheck(List<EntityDesc> entityDescList) {
        this.entityDescList = entityDescList;
    }

    public void loadByInputStream() {
        try (InputStream is = TextNameListCheck.class.getResourceAsStream("/name-list.txt")) {
            loadByInputStream(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadByInputStream(InputStream is) {
        Scanner scanner = new Scanner(is);
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("#")) {
                //log.info("line:{}", line);
                continue;
            }
            Matcher m0 = p0.matcher(line);
            //System.out.println(line);
            if (m0.find()) {
                //log.info("1:{},2:{},3:{}", m0.group(1), m0.group(2), m0.group(3));
                String arg1 = m0.group(1);
                String arg2 = m0.group(2);

                Directive directive = Directive.valueOf(arg1);
                MatchType matchType = MatchType.valueOf(arg2);

                //System.out.printf("====> %s,%s\n", connectType, matchType);

                EntityDesc entityDesc = new EntityDesc();
                entityDesc.setDirective(directive);
                entityDesc.setMatchType(matchType);
                if (matchType == MatchType.REGULAR_MATCHING) {
                    entityDesc.setPattern(Pattern.compile(m0.group(3)));
                } else if (matchType == MatchType.SUBNET) {
                    Matcher m1 = p1.matcher(m0.group(3));
                    if (m1.find()) {
                        String address = m1.group(1);
                        int cidrLen = Integer.parseInt(m1.group(2));

                        entityDesc.setAddress(address);
                        entityDesc.setCidrLen(cidrLen);
                    }
                }

                entityDesc.setData(m0.group(3));
                entityDescList.add(entityDesc);

            }
        }
    }

    public TextNameListCheck(InputStream is) {
        loadByInputStream(is);
    }


    @Override
    public Directive check(String input) {

        for (EntityDesc entityDesc : entityDescList) {
            Directive directive = entityDesc.test(input);
            if (directive != Directive.MISS) {
                return directive;
            }
        }
        return Directive.MISS;
    }

    @Override
    public Directive check(String name, Directive defaultDirective) {
        Directive directive = check(name);
        if (directive == Directive.MISS) {
            return defaultDirective;
        }
        return directive;
    }

    public List<EntityDesc> getEntityDescList() {
        return entityDescList;
    }

    public void setEntityDescList(List<EntityDesc> entityDescList) {
        this.entityDescList = entityDescList;
    }
}
