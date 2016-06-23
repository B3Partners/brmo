create or replace function f_datum(p_dat varchar2)
return date
is
    l_dat date;
begin
    if (substr(p_dat, 5,1) = '-')
    then
        l_dat := to_date(p_dat, 'yyyy-mm-dd');
    elsif  (substr(p_dat, 6,1) = '-')
    then
        l_dat := to_date(p_dat, 'dd-mm-yyyy');
    else  
        l_dat := null;  
    end if;
    return l_dat;
end;
/
