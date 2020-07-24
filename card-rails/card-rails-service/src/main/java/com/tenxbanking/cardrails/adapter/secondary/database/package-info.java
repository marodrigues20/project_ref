@TypeDefs({
    @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
package com.tenxbanking.cardrails.adapter.secondary.database;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;