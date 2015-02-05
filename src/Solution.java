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

    private class Team implements Comparable<Team> {
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
            if (this.first == m + 1) {
                this.first = m;
                this.size++;
                return true;
            }
            else if (this.last == m - 1){
                this.last = m;
                this.size++;
                return true;
            }
            return false;
        }

        public int extractMember() {
            if (this.size == 0) return Integer.MAX_VALUE;
            int ret = this.last;
            this.last--;
            this.size--;
            return ret;
        }

        public Team merge(Team m) {
            if (this.first == m.getLast() + 1) {
                return new Team(m.getFirst(),this.last);
            }
            else if (this.last == m.getFirst() - 1) {
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


        @Override
        public int compareTo(Team o) {
            return this.getSize() - o.getSize();
        }
    }

    private class TeamComparator implements Comparator<Team> {
        public TeamComparator() {}

        public int compare(Team a, Team b) {
            return a.getSize() - b.getSize();
        }

        public boolean equals(Object o) {
            return this == o;
        }
    }

    private class Teams {
        private int smallTeam = Integer.MAX_VALUE;
        private BinomialHeap<Team> teams;

        public Teams(int memberCount) {
            this.teams = new BinomialHeap<Team>();
        }

        public void addMember(int m) {
            this.teams.insert(new Team(m));
        }

        //attempt to merge the smallest teams into larger teams.  If the smallest team cannot be merged, stop
        public void mergeTeams() {
            BinomialHeap<Team> tmpTeams = new BinomialHeap<Team>();
            // Get the smallest team
            Team a = this.teams.deleteMin();
            Team b;
            //  While there are teams in the main heap
            while (this.teams.getMin() != null) {
                //Get smallest team left
                b = this.teams.deleteMin();
                Team newTeam = a.merge(b);
                //if teams merge, insert merged team into main heap
                if (newTeam != null) {
                    this.teams.insert(newTeam);
                    //reset the clock by merging teams with tmpTeams
                    this.teams.mergeHeap(tmpTeams);
                    tmpTeams = new BinomialHeap<Team>();
                    a = this.teams.deleteMin();  //get new min
                }
                //else put team into temp heap
                else tmpTeams.insert(b);
            }
            //merge main and temp heaps
            this.teams = tmpTeams;
            if (a != null) this.teams.insert(a);

            tmpTeams = new BinomialHeap<Team>();
            BinomialHeap<Team> heapCopy = new BinomialHeap<Team>();

            //Attempt to break smallest team and redistribute members
            a = this.teams.deleteMin();
            heapCopy.insert(new Team(a.getFirst(),a.getLast()));
            int m;
            while ((m = a.extractMember()) != Integer.MAX_VALUE) { //Go until team a is empty
                //get smallest team in heap
                b = this.teams.deleteMin();
                //if no more teams in heap, then this member can't be reassigned
                if (b == null) {
                    this.teams = heapCopy; //These teams can't be fully optimized, restore state
                    return; //finished
                }
                //put copy onto copy heap
                heapCopy.insert(new Team(b.getFirst(),b.getLast()));
                //try to add member to min team
                if (b.addMember(m)) {
                    this.teams.insert(b);   //insert into min team
                }
                //otherwise put this team aside
                else tmpTeams.insert(b);
            }
            //Smallest team has been broken up
            this.teams.mergeHeap(tmpTeams);

        }

        public int getSmallTeam() {
            return this.teams.getMin().getSize();
        }
    }

    private class BinomialHeap<E extends Comparable<E>> {

        //Binomial Heap node helper class
        private class BinomialHeapNode<T extends Comparable<T>> implements Comparable<BinomialHeapNode<T>> {

            private T key;
            private int size;
            private int order;
            private PriorityQueue<BinomialHeapNode<T>> children;

            public BinomialHeapNode(T k){
                this.key = k;
                this.order = 0;
                this.size = 1;
                this.children = new PriorityQueue<BinomialHeapNode<T>>();
            }

            public void addChild(BinomialHeapNode<T> node) {
                children.add(node);
                this.order++;
                this.size += node.size;
            }

            public BinomialHeapNode<T> getMin() {
                BinomialHeapNode<T> child = this.children.poll();
                if (child != null) this.size -= child.size;
                return child;
            }

            @Override
            public int compareTo(BinomialHeapNode<T> o) {
                return this.getKey().compareTo(o.getKey());
            }
            public T getKey() {
                return key;

            }

            public void setKey(T key) {
                this.key = key;
            }

            public void setOrder(int order) {
                this.order = order;
            }

            public PriorityQueue<BinomialHeapNode<T>> getChildren() {
                return children;
            }

            public void setChildren(PriorityQueue<BinomialHeapNode<T>> children) {
                this.children = children;
            }

            public int getOrder() {
                return order;
            }

            public int getSize() {
                return this.size;
            }
        }
        //End Binomial Heap Node Helper class

        private Map<Integer,BinomialHeapNode<E>> forest;
        private PriorityQueue<BinomialHeapNode<E>> min;
        private int size;

        //Public API

        public BinomialHeap() {
            this.forest = new HashMap<Integer, BinomialHeapNode<E>>();
            this.size = 0;
        }
        public BinomialHeap(E k) {
            BinomialHeapNode<E> node = new BinomialHeapNode<E>(k);
            this.forest = new HashMap<Integer,BinomialHeapNode<E>>();
            forest.put(0, node);
            this.size = 1;
            findMin();
        }

        public BinomialHeap(BinomialHeapNode<E> tree) {
            this.forest = new HashMap<Integer,BinomialHeapNode<E>>();
            forest.put(tree.getOrder(),tree);
            this.size = tree.getSize();
            findMin();
        }

        public E getMin() {
            if (this.size == 0) return null;
            return this.min.peek().getKey();
        }

        public E deleteMin() {
            if (this.size == 0) return null;
            BinomialHeapNode<E> ret = this.min.poll();
            this.size -= ret.getSize();
            this.forest.remove(ret.getOrder());
            BinomialHeapNode<E> tree;
            while ( (tree = ret.getMin()) != null){
                this.mergeHeap(new BinomialHeap<E>(tree));
            }
            return ret.getKey();
        }

        public void insert(E k) {
            BinomialHeap<E> q = new BinomialHeap<E>(k);
            this.mergeHeap(q);
        }

        private void mergeHeap(BinomialHeap<E> heap) {
            //Iterate through the merging heap's forest
            for (int order : heap.forest.keySet()){
                //check if my forest contains tree of this order
                if (this.forest.containsKey(order)) {
                    //Merge merging heap k-order tree with my k-order tree
                    BinomialHeapNode<E> newTree = mergeTree(this.forest.get(order), heap.forest.get(order));
                    //check if my forest contains a k+1 order tree
                    while (this.forest.containsKey(newTree.getOrder())) {
                        int newOrder = newTree.getOrder(); //order k+1
                        //Merge newTree with existing k+1 order tree
                        newTree = mergeTree(newTree,this.forest.get(newOrder));
                        //remove old k+1-order tree
                        this.forest.remove(newOrder);
                        //repeat until unused order found
                    }
                    //remove old k-order tree
                    this.forest.remove(order);
                    //add new tree to my forest
                    this.forest.put(newTree.getOrder(),newTree);
                }
                // if no k-order tree, add directly to forest
                else this.forest.put(order,heap.forest.get(order));
            }
            this.size += heap.size;
            findMin(); //rebuild min list
        }

        //End public API

        //Private helper methods
        private BinomialHeapNode<E> mergeTree(BinomialHeapNode<E> p, BinomialHeapNode<E> q) {
            //If p is less than q
            if (p.compareTo(q) <= 0) {
                p.addChild(q);
                return p;
            }
            else {
                q.addChild(p);
                return q;
            }
        }

        private void findMin() {
            min = new PriorityQueue<BinomialHeapNode<E>>();
            for (BinomialHeapNode<E> tree : forest.values()) {
                min.add(tree);
            }
        }

    }

}
