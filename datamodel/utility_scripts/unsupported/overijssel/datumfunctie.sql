CREATE OR REPLACE FUNCTION f_datum(
    p_dat VARCHAR2)
  RETURN DATE
IS
  l_dat DATE;
BEGIN
  IF (SUBSTR(p_dat, 5,1)    = '-') THEN
    l_dat                  := to_date(p_dat, 'yyyy-mm-dd');
  elsif (SUBSTR(p_dat, 6,1) = '-') THEN
    l_dat                  := to_date(p_dat, 'dd-mm-yyyy');
  ELSE
    l_dat := NULL;
  END IF;
RETURN l_dat;
END;
/
