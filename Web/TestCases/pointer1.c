#include "header.inc"

int main() {
  int a;
  int b;
  int c;
  int *p;
  int d;
  p = &c;
  *p = 42;
  RETURN (c);
}
