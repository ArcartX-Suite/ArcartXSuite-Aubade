package xuanmo.aubade.core.island;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IslandGridTest {

  @Test
  void computesSpiralCentersUsingConfiguredSpacing() {
    IslandGrid grid = new IslandGrid(200);

    assertArrayEquals(new int[] {0, 0}, grid.getCenter(0));
    assertArrayEquals(new int[] {200, 0}, grid.getCenter(1));
    assertArrayEquals(new int[] {200, 200}, grid.getCenter(2));
    assertArrayEquals(new int[] {0, 200}, grid.getCenter(3));
    assertArrayEquals(new int[] {-200, 200}, grid.getCenter(4));
  }

  @Test
  void nextIndexAdvancesMonotonically() {
    IslandGrid grid = new IslandGrid(128);

    assertEquals(0, grid.nextIndex());
    assertEquals(1, grid.nextIndex());
    assertEquals(2, grid.nextIndex());
  }
}
