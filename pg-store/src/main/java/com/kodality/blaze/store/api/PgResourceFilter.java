package com.kodality.blaze.store.api;

import com.kodality.blaze.util.sql.SqlBuilder;

public interface PgResourceFilter {

  SqlBuilder filter(SqlBuilder builder, String alias);
}
