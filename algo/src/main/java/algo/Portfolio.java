package algo;

import java.util.Collection;

public interface Portfolio {
    double get(String currency);

    Collection<String> currencies();
}
