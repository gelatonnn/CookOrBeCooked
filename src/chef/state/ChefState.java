package chef.state;

import chef.Chef;
import input.Command;  // sesuai diagram
                      // pindah ke package yang benar kalau beda

public abstract class ChefState {

    public abstract void handleInput(Chef chef, Command cmd);

    public abstract void update(Chef chef);
}
