set define off;

select 1 from dual;

-- laat zien wat de limieten zijn voor de database wb kolom/tabelnaam lengte
-- 128 bytes voor kolomnaam en tabelnaam
select * from all_tab_columns where table_name = 'ALL_TAB_COLUMNS';