import java.io.*;
import java.util.*;
import java.text.*;
import java.math.*;
import java.util.regex.*;

public class Solution {

    public static void main(String[] args) {
       Solution solution = new Solution();
    }
                                             
    public Solution() {
         Scanner in = new Scanner(System.in);
        int testNos;
        testNos = in.nextInt();
        
        Map<Integer,List<Integer>> tests = new HashMap<Integer,List<Integer>>(testNos);
        
        for (int i=0; i<testNos; i++) {
            int testSize = in.nextInt();
            List<Integer> testSet = new ArrayList<Integer>(testSize);
            
            for (int j = 0; j < testSize; j++) {
                testSet.add(in.nextInt());
            }
            tests.put(i,testSet);
        }
        
        for (int test : tests.keySet()) {
            System.out.println(formTeams(tests.get(test)));
        }
    }                                        
    
    
    private int formTeams(List<Integer> members) {
        if (members.size() == 0) return 0;
        
        Teams teams = new Teams(members.size());
        for (int memberSkill : members) {
            teams.addMember(memberSkill);
        }
        teams.mergeTeams();
        return teams.getSmallTeam();
    }
    
    private class Team {
        private int first;
        private int last;
        private int size;
        
        public Team(int first) {
            this.first = first;
            this.last = first;
            this.size = 1;
        }
        
        public Team(int first,int last) {
            this.first = first;
            this.last = last;
            this.size = last - first + 1;
        }
        
        public boolean addMember(int m) {
            if (this.first == m - 1) {
                this.first = m;
                this.size++;
                return true;
            }
            else if (this.last == m + 1){
                this.last = m;
                this.size++;
                return true;
            }
            return false;
        }
        
        public void merge(Team m) {
            if (this.first == m.getLast() - 1) {
                return new Team(m.getFirst(),this.last);
            }
            else if (this.last == m.getFirst() + 1) {
                return new Team(this.getFirst(),m.getLast());
            }
            else return null;
        }
        
        public int getFirst(){
            return this.first;
        }
        public int getLast() {
            return this.last;
        }
        public int getSize() {
            return this.size;
        }
        
        
    }
    
    private class TeamComparator implements Comparator<Team> {
        public TeamComparator() {}
        
        public int compare(Team a, Team b) {
            return a.getSize() - b.getSize();
        }
        
        public void equals(Object o) {
            return this == o;
        }
    }
    
    private class Teams {
        private int smallTeam = Integer.MAX_VALUE;
        private PriorityQueue<Team> teams;
        
        public void Teams(int memberCount) {
            this.teams = new PriorityQueue<Team>(memberCount/2, new TeamComparator());
        }
        
        public void addMember(int m) {
            Team newTeam;
            Team PriorityQueue<Team> newTeams = new PriorityQueue<Team>(this.teams.size()+1, new TeamComparator());
            boolean unassigned = true;
            while (this.teams.peek() != null) {
                newTeam = this.teams.poll();
                if (unassigned) unassigned = !newTeam.addMember(m);
                newTeams.add(newTeam);
            }
            if (unassigned) {
                newTeams.add(new Team(m));
            }
            this.teams = newTeams;
        }
        
        public void mergeTeams() {
            PriorityQueue<Team> newTeams = new PriorityQueue<Team>(this.teams.size(), new TeamComparator());
            while(true) {
                Team a = this.teams.poll();
                PriorityQueue<Team> tmpTeams = new PriorityQueue<Team>(this.teams.size(), new TeamComparator());
                while(this.teams.peek() != null) {
                    Team b = this.teams.poll();
                    Team merge = a.merge(b);
                    if (merge != null) {
                        this.teams.add(merge);
                        a = this.teams.poll();
                        continue;
                    }
                    tmpTeams.add(b);
                }
                this.teams = tmpTeams;
                newTeams.add(a)
            }
            this.teams = newTeams;
        }
        
        public int getSmallTeam() {
            return this.teams.peek.size();
        }
    }
    
}
