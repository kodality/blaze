package com.kodality.blaze.search.api;

import com.kodality.blaze.util.sql.SqlBuilder;

public interface PgResourceSearchFilter {

  SqlBuilder filter(SqlBuilder builder, String alias);
}
