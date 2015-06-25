/*
	materialized views, minimaal postgresql 9.3 vereist
*/

create materialized view 			m_adres_totaal as select * from v_adres_totaal;
create unique index ui_mat on m_adres_totaal(fid);
comment on materialized view 	m_adres_totaal is 'uses v_adres_totaal';

create materialized view 			m_verblijfsobject_alles as select * from v_verblijfsobject_alles ;
create unique index ui_mva on m_verblijfsobject_alles(fid);
comment on materialized view 	m_verblijfsobject_alles is 'uses v_verblijfsobject_alles';

-- mview om kaart te maken met percelen die 1 of meerdere appartementen hebben
create materialized view 			m_bd_kad_perceel_met_app as select * from v_bd_kad_perceel_met_app ;
create unique index ui_dkpma on m_bd_kad_perceel_met_app(perceel_identif);
comment on materialized view 	m_bd_kad_perceel_met_app is 'uses v_bd_kad_perceel_met_app';

-- mview om app_re' s bij percelen op te zoeken
create materialized view 			m_bd_app_re_bij_perceel as select * from v_bd_app_re_bij_perceel ;
--create unique index ui_barbp on m_bd_app_re_bij_perceel(sc_kad_identif);
comment on materialized view 	m_bd_app_re_bij_perceel is 'uses v_bd_app_re_bij_perceel';

-- rest BRK mviews todo