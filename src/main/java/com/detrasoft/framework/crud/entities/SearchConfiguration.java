package com.detrasoft.framework.crud.entities;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchConfiguration {
    private String id;
    private String title;
    private String from;
    private String where;
    private String groupBy;
    private String orderBy;
    private List<SearchField> columns;
}
