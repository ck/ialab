package ialab;

public class Num
{

   private Num() {}

    public static int gcd(int x, int y)
    {
      if (x < 0) x = -x;
      if (y < 0) y = -y;

      int r;
      while (y != 0)
      {
         r = x % y;
         x = y;
         y = r;
      }

      return x;
   }

}
