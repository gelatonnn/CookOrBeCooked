package chef.state;

import chef.Chef;
import chef.Projectile;
import input.Command;

public class ThrowingState extends ChefState {

    private Projectile activeProjectile;

    public ThrowingState(Projectile p) {
        this.activeProjectile = p;
    }

    @Override
    public void handleInput(Chef chef, Command cmd) {
        // biasanya ignore input
    }

    @Override
    public void update(Chef chef) {
        // gerakkan projectile
        // kalau projectile sudah selesai → kembali ke IdleState
    }
}
