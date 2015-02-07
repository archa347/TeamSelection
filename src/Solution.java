/**
 * Created by drgalleg on 2/3/2015.
 */
import java.io.*;
import java.util.*;
import java.text.*;
import java.math.*;
import java.util.regex.*;

public class Solution {

    public static void main(String[] args) {
        Solution solution = new Solution();
    }

    private List<TeamMember> members;
    private List<Team> teams;

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
            System.out.println(smallestTeam(tests.get(test)));
        }
    }


    private int smallestTeam(List<Integer> members) {
        if (members.size() == 0) return 0;
        this.members = initializeMembers(members);
        this.teams = initializeTeams(this.members);
        return getSmallestTeam();
    }

    private int getSmallestTeam() {
        PriorityQueue<Team> minTeam = new PriorityQueue<Team>();
        minTeam.addAll(this.teams);
        boolean changed = true;
        while (changed) {
            Team min = minTeam.poll();

        }
    }

    private List<Team> initializeTeams(List<TeamMember> members) {
        List<Team> teams = new ArrayList<Team>(members.size());
        for (TeamMember member : members) {
            Team team = new Team(member.getId());
            teams.add(team);
            try {
                member.setTeam(team);
            } catch (TeamMember.NullTeamException e) {
                System.out.println("Null team when setting a team to member " + member.getId());
                e.printStackTrace();
                System.exit(100);
            } catch (Team.DeleteMemberException e) {
                System.out.println("Problem when setting a team to member " + member.getId());
                e.printStackTrace();
                System.exit(100);
            } catch (Team.AddMemberException e) {
                System.out.println("Problem when team when setting a team to member " + member.getId());
                e.printStackTrace();
                System.exit(100);
            }
        }
        return teams;
    }

    private List<TeamMember> initializeMembers(List<Integer> members) {
        Map<Integer,List<TeamMember>> memberSkills = new HashMap<Integer, List<TeamMember>>();
        List<TeamMember> teamMembers = new ArrayList<TeamMember>(members.size());

        int id = 0;
        for (int skill : members) {
            TeamMember member = new TeamMember(id,skill);
            id++;

            if (!memberSkills.containsKey(skill)) {
                memberSkills.put(skill, new ArrayList<TeamMember>());
            }
            memberSkills.get(skill).add(member);
            teamMembers.add(member);

            try {
                for (TeamMember adj : memberSkills.get(skill - 1)) {
                    member.linkAdjacent(adj);
                }
            } catch (NullPointerException e) {
                //Let it continue
            } catch (TeamMember.InvalidAdjacentException e) {
                System.out.println("TeamMember skill does not match list");
                e.printStackTrace();
                System.exit(100);
            }

            try {
                for (TeamMember adj : memberSkills.get(skill + 1)) {
                    member.linkAdjacent(adj);
                }
            } catch (NullPointerException e) {
                //let it continue
            } catch (TeamMember.InvalidAdjacentException e) {
                System.out.println("TeamMember skill does not match list");
                e.printStackTrace();
                System.exit(100);
            }
        }

        return teamMembers;
    }

    public class Team implements Comparable<Team> {


        private int id;
        private Deque<TeamMember> members = new ArrayDeque<TeamMember>();

        public Team(int id) {
            this.id = id;
        }

        public int getSize() {
            return this.members.size();
        }

        public int getId() {
            return id;
        }

        public void addMember(int skill, TeamMember m) throws AddMemberException {
            if ( m.getSkill() == this.members.peekFirst().getSkill() - 1) {
                members.addFirst(m);
            }
            else if ( m.getSkill() == this.members.peekLast().getSkill() + 1) {
                members.addLast(m);
            }
            else throw new AddMemberException();
        }

        public void delMember(TeamMember m) throws DeleteMemberException {
            if ( m == this.members.peekFirst()) {
                members.pollFirst();
            }
            else if ( m == this.members.peekLast()) {
                members.pollLast();
            }
            else throw new DeleteMemberException();
        }

        public int compareTo(Team t) {
            return this.getSize() - t.getSize();
        }

        private class AddMemberException extends Exception {

        }

        private class DeleteMemberException extends Exception {
        }
    }

    public class TeamMember {
        private int id;
        private int skill;
        private Team team;
        private List<NodeLink> links = new ArrayList<NodeLink>();

        public TeamMember(int id, int skill) {
            this.id = id;
            this.skill = skill;
        }


        public int getTeamSize() {
            return this.team.getSize();
        }

        public int getSkill() {
            return this.skill;
        }

        public void linkAdjacent(TeamMember m) throws InvalidAdjacentException {
            if (this.getSkill() == m.getSkill() + 1 || this.getSkill() == m.getSkill()-1) {
                links.add(new NodeLink(this,m));
            }
            else throw new InvalidAdjacentException();
        }

        public void acceptLink(NodeLink n) {
            this.links.add(n);
        }

        public int getId() {
            return this.id;
        }

        public Team getTeam() {
            return team;
        }

        public void setTeam(Team team) throws NullTeamException, Team.DeleteMemberException, Team.AddMemberException {
            if (team == null) {
                throw new NullTeamException();
            }
            try {
                if (this.team != null)
                    this.team.delMember(this);
            } catch (Team.DeleteMemberException e) {
                throw e;
            }

            this.team = team;
            try {
                this.team.addMember(this.getSkill(), this);
            } catch (Team.AddMemberException e) {
                throw e;
            }
        }

        public class InvalidAdjacentException extends Exception {
        }

        private class NullTeamException extends Exception {
        }
    }

    public class NodeLink implements Comparable<NodeLink> {

        private TeamMember node;
        private NodeLink link;

        public NodeLink(TeamMember a, TeamMember b) {
            this.node = a;
            linkTeamMember(b);
        }

        public NodeLink(TeamMember m) {
            this.node = m;
        }

        public int getWeight() throws IncompleteLinkException {
            try {
                return this.node.getTeamSize() - this.link.getNode().getTeamSize();
            } catch (NullPointerException e) {
                throw new IncompleteLinkException();
            }
        }


        public void linkTeamMember(TeamMember m) {
            NodeLink newNode = new NodeLink(m);
            this.makeLink(newNode);
        }

        public void makeLink(NodeLink n) {
            this.link = n;
            n.acceptLink(this);
        }

        public TeamMember getNode() {
            return this.node;
        }

        public TeamMember getAdjacent() throws IncompleteLinkException {
            try {
                return this.link.getNode();
            } catch (NullPointerException e) {
                throw new IncompleteLinkException();
            }
        }

        public void acceptLink(NodeLink n) {
            this.link = n;
            this.node.acceptLink(n);
        }

        public void migrateTo() throws IncompleteLinkException, Team.DeleteMemberException, Team.AddMemberException {
            try {
                this.node.setTeam(this.link.getNode().getTeam());
            } catch (TeamMember.NullTeamException e) {
                System.out.println("Null team when adding a team to member " + this.node.getId());
                e.printStackTrace();
                System.exit(100);
            } catch (NullPointerException e) {
                throw new IncompleteLinkException();
            }
        }

        public void migrateNodeFrom() throws IncompleteLinkException, Team.DeleteMemberException, Team.AddMemberException {
            try {
                this.link.migrateTo();
            } catch (NullPointerException e) {
                throw new IncompleteLinkException();
            }
        }

        public int compareTo(NodeLink n) {
            try {
                return this.getWeight() - this.link.getWeight();
            } catch (IncompleteLinkException e) {
                System.out.println("No end on link from member " + this.node.getId());
            }

            return Integer.MAX_VALUE;
        }

        private class IncompleteLinkException extends Exception {
        }
    }
}
