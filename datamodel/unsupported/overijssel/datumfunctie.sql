CREATE OR REPLACE FUNCTION F_DATUM(p_dat VARCHAR2)
  RETURN DATE
IS
  l_dat DATE;
BEGIN
  IF (SUBSTR(p_dat, 8,1)    = '-') THEN
    l_dat := to_date(p_dat, 'yyyy-mm-dd');
  ELSIF (SUBSTR(p_dat, 6,1) = '-') THEN
    l_dat := to_date(p_dat, 'dd-mm-yyyy');
  -- datum als number; bijv. subject geb. datum
  ELSIF (p_dat > 19000000) THEN
    l_dat := to_date(p_dat, 'yyyymmdd');
  ELSE
    l_dat := NULL;
  END IF;
RETURN l_dat;
END;
/
