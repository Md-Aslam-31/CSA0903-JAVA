import java.util.*;

public class LeaderboardDemo {
    static final Comparator<Team> BY_RANK=Comparator.comparingInt((Team t)->t.points).reversed().thenComparing(Comparator.comparingInt((Team t)->t.goalDiff).reversed()).thenComparing(t->t.name);
    static final class Team {final String name;final int points,goalDiff;Team(String n,int p,int g){name=n;points=p;goalDiff=g;}public String toString(){return name+"("+points+","+goalDiff+")";}}
    static class Leaderboard<T>{private final List<T>items=new ArrayList<>();private final Comparator<? super T>order;Leaderboard(Comparator<? super T>c){order=c;}void add(T item){items.add(Objects.requireNonNull(item));}void addAll(Collection<? extends T> values){items.addAll(values);}List<T>ranking(){List<T>copy=new ArrayList<>(items);copy.sort(order);return copy;}int size(){return items.size();}}
    public static void main(String[]args){Leaderboard<Team>b=new Leaderboard<>(BY_RANK);b.add(new Team("Panthers",9,4));b.add(new Team("Eagles",9,6));b.add(new Team("Cobras",6,1));b.add(new Team("Bisons",9,4));System.out.println("Ranking: "+b.ranking());System.out.println("Stored size unchanged: "+b.size());
        Comparator<Team> faulty=(x,y)->y.points-x.points;Team high=new Team("High",Integer.MAX_VALUE,0),low=new Team("Low",Integer.MIN_VALUE,0);System.out.println("Subtraction comparator overflow probe, sign: "+Integer.signum(faulty.compare(high,low))+" (wrong; high should rank first)");
        // Stable-sort alternative: least-to-most significant keys, each descending key reversed.
        List<Team> stable=new ArrayList<>(List.of(new Team("Panthers",9,4),new Team("Eagles",9,6),new Team("Cobras",6,1),new Team("Bisons",9,4)));
        stable.sort(Comparator.comparing(t->t.name));stable.sort(Comparator.comparingInt((Team t)->t.goalDiff).reversed());stable.sort(Comparator.comparingInt((Team t)->t.points).reversed());System.out.println("Three stable passes: "+stable);
    }
}
