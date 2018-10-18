#include <stdio.h>
#include <wiringPi.h>

int main(int argc, char *argv[])
{
  int i;
  wiringPiSetup () ;
  pinMode(0, INPUT);
  while(1) {
    printf("Waiting for reset\n");
    while(digitalRead(0) == 1);
    printf("Waiting for event\n");
    while(digitalRead(0) == 0);
    printf("Alarm\n");
  }
  /*NOTREACHED*/
  return 0 ;
}
