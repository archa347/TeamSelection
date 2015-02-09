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

    private void consolidateTeams(List<TeamMember> members) {
        List<TeamMember> memberForest = new ArrayList<TeamMember>();
        List<TeamMember> memberCopy = new ArrayList<TeamMember>();
        memberCopy.addAll(members);

        while (!memberCopy.isEmpty()) {
            TeamMember member = memberCopy.remove(0);
            memberForest.add(member);
            try {
                NodeLink link = member.getMinNeighbor();
                TeamMember neighbor = link.getAdjacent();
                memberCopy.remove(neighbor);
                link.migrateTo();

            } catch (NodeLink.IncompleteLinkException e) {
                e.printStackTrace();
                System.exit(100);
            } catch (TeamMember.NoLinkException e) {
                e.printStackTrace();
            } catch (TeamMember.NoExternalLinkException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Team> initializeTeams(List<TeamMember> members) {
        List<Team> teams = new ArrayList<Team>(members.size());
        for (TeamMember member : members) {
            Team team = new Team(member.getId());
            teams.add(team);
            try {
                member.setTeam(team);
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
        private ArrayList<TeamMember> members = new ArrayList<TeamMember>();

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
            if ( m.getSkill() == this.members.get(0).getSkill() - 1) {
                members.add(0,m);
            }
            else if ( m.getSkill() == this.members.get(this.members.size()-1).getSkill() + 1) {
                members.add(m);
            }
            else throw new AddMemberException();
        }

        public void delMember(TeamMember m) throws DeleteMemberException {
            List<TeamMember> teamA = this.members.subList(0,this.members.indexOf(m));

            Team lowerTeam = new Team((new Random()).nextInt());

            this.members.removeAll(teamA);

            for (TeamMember member : teamA) {
                try {
                    member.setTeam(lowerTeam);
                } catch (AddMemberException e) {
                    e.printStackTrace();
                }
            }

            this.members.remove(m);

        }

        public TeamMember first() {
            return this.members.get(0);
        }

        public TeamMember last() {
            return this.members.get(members.size()-1);
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

        public Team getTeam() throws NullTeamException {
            if (team == null) throw new NullTeamException();
            return team;
        }

        public void setTeam(Team team) throws Team.AddMemberException {
            this.team = team;
            try {
                this.team.addMember(this.getSkill(), this);
            } catch (Team.AddMemberException e) {
                e.printStackTrace();
                System.exit(100);
            }
        }

        public NodeLink getMinNeighbor() throws NoLinkException, NoExternalLinkException{
            try {
                PriorityQueue<NodeLink> minLink = new PriorityQueue<NodeLink>(this.links);
                if (minLink.peek().getWeight() == 0) throw new NoExternalLinkException();
                return minLink.poll();
            } catch (NullPointerException e) {
                throw new NoLinkException();
            } catch (NodeLink.IncompleteLinkException e) {
                e.printStackTrace();
                System.exit(100);
            }
            return null;
        }

        public NodeLink getMaxNeighbor() throws NoLinkException, NoExternalLinkException {
            try {
                PriorityQueue<NodeLink> maxLink = new PriorityQueue<NodeLink>(this.links.size(), new NodeLinkMaxComparator());
                if (maxLink.peek().getWeight() == 0) throw new NoExternalLinkException();
                return maxLink.poll();
            } catch (NullPointerException e) {
                throw new NoLinkException();
            } catch (NodeLink.IncompleteLinkException e) {
                e.printStackTrace();
                System.exit(100);
            }
            return null;
        }

        public class InvalidAdjacentException extends Exception {
        }

        private class NullTeamException extends Exception {
        }

        private class NoLinkException extends Throwable {
        }

        private class NoExternalLinkException extends Throwable {
        }
    }

    public class NodeLinkMaxComparator implements Comparator<NodeLink> {

        @Override
        public int compare(NodeLink o1, NodeLink o2) {
            return -(o1.compareTo(o2));
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

        public void migrateTo() throws IncompleteLinkException {
            try {
                this.node.getTeam().delMember(this.node);
                this.node.setTeam(this.link.getNode().getTeam());
            } catch (TeamMember.NullTeamException e) {
                System.out.println("Null team when adding a team to member " + this.node.getId());
                e.printStackTrace();
                System.exit(100);
            } catch (NullPointerException e) {
                throw new IncompleteLinkException();
            } catch (Team.DeleteMemberException e) {
                e.printStackTrace();
                System.exit(100);
            } catch (Team.AddMemberException e) {
                e.printStackTrace();
                System.exit(100);
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
