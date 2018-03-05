package com.yiguo.offer100.search.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import com.yiguo.offer100.search.bean.Job;
import com.yiguo.offer100.search.util.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

/**
 * Job搜索仓库类
 *
 * @author wanghuan
 * @date 2018-01-18
 */
@Repository
public class JobSearchRepository {

    @Autowired
    @Qualifier("jobHttpSolrClient")
    private HttpSolrClient httpSolrClient;

    public void save(List<Job> jobs) throws SolrServerException, IOException {
        // 添加多个
        Collection<SolrInputDocument> docs = new ArrayList<>();
        jobs.forEach(job->{
                SolrInputDocument doc = new SolrInputDocument();
                Map<String, Object> map = BeanUtils.objectToMap(job);
                map.forEach((t, u) -> {
                    if (u != null && !StringUtils.equals(t, "key")) {
                        doc.addField(t, u);
                    }
                });
                docs.add(doc);
            });
        httpSolrClient.add(docs);
        httpSolrClient.commit();

    }

    public void save(Job job) throws SolrServerException, IOException {
        SolrInputDocument doc = new SolrInputDocument();
        Map<String, Object> map = BeanUtils.objectToMap(job);
        map.forEach((t, u) -> {
            if (u != null && !StringUtils.equals(t, "key")) {
                doc.addField(t, u);
            }
        });
        httpSolrClient.add(doc);
        httpSolrClient.commit();
    }

    public void deleteById(String id) throws SolrServerException, IOException {
        httpSolrClient.deleteById(id);
        httpSolrClient.commit();
    }

    public void deleteByIds(List<String> id) throws SolrServerException, IOException {
        httpSolrClient.deleteById(id);
        httpSolrClient.commit();
    }

    public List<Job> search(Job job, Integer start, Integer size) throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery();
        StringBuilder queryStringBuilder = new StringBuilder("*:*");
        Map<String, Object> map = BeanUtils.objectToMap(job);
        map.forEach((t, u) -> {
            Optional.ofNullable(u).ifPresent(v -> {
                if (v instanceof List) {
                    List<String> list = (List<String>) v;
                    if(!StringUtils.equals(t, "key")) {
                        list.forEach(l -> queryStringBuilder.append(" AND ").append(t).append(":").append(l));
                    }else {
                        list.forEach(l -> queryStringBuilder.append(" AND ").append("all_text_pinyin").append(":").append(l));
                    }
                } else {
                    if(StringUtils.equalsAny(t, "title", "enterprise")) {
                        queryStringBuilder.append(" AND ").append(t).append("_pinyin").append(":").append(v);
                    }else {
                        queryStringBuilder.append(" AND ").append(t).append(":").append(v);
                    }
                }
            });
        });
        // q 查询字符串，如果查询所有*:*
        query.set("q", queryStringBuilder.toString());
        // fq 过滤条件，过滤是基于查询结果中的过滤
        // query.set("fq", "catalogname:*驰*");
        // fq 此过滤条件可以同时搜索出奔驰和宝马两款车型，但是需要给catalogname配置相应的分词器
        // query.set("fq", "catalogname:奔驰宝马");
        // sort 排序，请注意，如果一个字段没有被索引，那么它是无法排序的
        query.set("sort", "rank desc");
        // start row 分页信息，与mysql的limit的两个参数一致效果
        if (start != null && size != null)

        {
            query.setStart(start);
            query.setRows(size);
        }

        // fl 指定返回那些字段内容，用逗号或空格分隔多个
        query.set("fl", "id,enterprise,title,nature,zone,category,wage,education,rank");
        return httpSolrClient.query(query).getBeans(Job.class);
    }

}