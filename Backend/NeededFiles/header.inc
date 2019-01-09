#ifdef ACTUAL
#include <stdio.h>
#define RETURN(e) \
  return (printf("%d\n", e), 0)
#define RETURNFLOAT(e) \
  return (printf("%f\n", e), 0)
#else
#define RETURN(e) \
  return e  
#define RETURNFLOAT(e) \
  float ___f = e; \
  return *((int*)&___f)  
#endif
