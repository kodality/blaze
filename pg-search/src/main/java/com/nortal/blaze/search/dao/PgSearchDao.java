package com.nortal.blaze.search.dao;

import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.core.model.search.QueryParam;
import com.nortal.blaze.core.model.search.SearchCriterion;
import com.nortal.blaze.search.sql.SqlToster;
import com.nortal.blaze.store.dao.ResourceRowMapper;
import com.nortal.blaze.util.sql.SqlBuilder;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.springframework.jdbc.core.JdbcTemplate;

@Component(immediate = true, service = PgSearchDao.class)
public class PgSearchDao {
  private static final Logger LOG = LogManager.getLogger(PgSearchDao.class);
  @Reference
  private JdbcTemplate jdbcTemplate;

  public Integer count(SearchCriterion criteria) {
    SqlBuilder sb = new SqlBuilder("SELECT count(1) FROM " + criteria.getType().toLowerCase() + " base ");
    sb.append(joins(criteria));
    sb.append(" WHERE 1=1");
    sb.append(criteria(criteria));
    LOG.info(sb.getSqlForFun());
    return jdbcTemplate.queryForObject(sb.getSql(), Integer.class, sb.getParams());
  }

  public List<ResourceVersion> search(SearchCriterion criteria) {
    SqlBuilder sb = new SqlBuilder("SELECT base.* FROM " + criteria.getType().toLowerCase() + " base ");
    sb.append(joins(criteria));
    sb.append(" WHERE 1=1");
    sb.append(criteria(criteria));
    sb.append(order(criteria));
    sb.append(limit(criteria));
    LOG.info(sb.getSqlForFun());
    return jdbcTemplate.query(sb.getSql(), sb.getParams(), new ResourceRowMapper());
  }

  private SqlBuilder limit(SearchCriterion criteria) {
    SqlBuilder sb = new SqlBuilder();
    Integer limit = criteria.getCount();
    Integer page = criteria.getPage();
    Integer offset = limit * (page - 1);
    return sb.append(" LIMIT ? OFFSET ?", limit, offset);
  }

  private SqlBuilder joins(SearchCriterion criteria) {
    SqlBuilder sb = new SqlBuilder();
    sb.append(SqlToster.chain(criteria.getChains(), "base"));
    return sb;
  }

  private SqlBuilder criteria(SearchCriterion criteria) {
    SqlBuilder sb = new SqlBuilder();
    for (QueryParam param : criteria.getConditions()) {
      SqlBuilder peanut = SqlToster.condition(param, "base");
      if (peanut != null) {
        sb.and("(").append(peanut).append(")");
      }
    }
    sb.and("base.sys_status = 'A'");
    return sb;
  }

  private SqlBuilder order(SearchCriterion criteria) {
    SqlBuilder sb = new SqlBuilder();
    boolean first = true;
    for (QueryParam param : criteria.getResultParams(SearchCriterion._SORT)) {
      SqlBuilder sql = SqlToster.order(param, "base");
      if (sql == null) {
        continue;
      }
      sb.append(first ? " ORDER BY " : ",");
      sb.append(sql);
      first = false;
    }
    return sb;
  }

}
