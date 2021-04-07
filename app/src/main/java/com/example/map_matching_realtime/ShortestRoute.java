package com.example.map_matching_realtime;

import java.util.ArrayList;

public class ShortestRoute {
    MainActivity mainActivity = new MainActivity();

    public ShortestRoute(){;}

    public static double dijkstra_length(RoadNetwork roadNetwork, ArrayList<AdjacentNode> heads, int start, int end){
        double length = 0;
        return length;
    }

    //A가 크면 1, B가 크면 2를 반환하는 함수
    public static int min(double A, double B){
        if(A>B) return 1;
        return 2;
    }

    public ArrayList<Integer> dijkstra(RoadNetwork roadNetwork, ArrayList<AdjacentNode> heads, int start, int end){
        ArrayList<Integer> route = new ArrayList<>();

        int node_num = roadNetwork.nodeArrayList.size();
        double INF = 1000000.0;
        double[][] a = new double[node_num][node_num]; //전체 거리 그래프
        int path[] = new int[node_num];

        /* 전체 거리 그래프 초기화 */
        for(int i=0;i<node_num;i++){ // 전체 거리 그래프 전체 INF로 초기화
            path[i]=-1;
            for(int j=0;j<node_num;j++){
                a[i][j]=INF;
            }
        }
        AdjacentNode head = heads.get(start);
        while(head.getNextNode()!=null){
            head = head.getNextNode();
            path[head.getNode().getNodeID()]=start;
        }

        for(int i=0;i<node_num;i++){ //실제 이어져 있는 LINK는 WEIGHT로 전체 거리 그래프 재 초기화
            head = heads.get(i);
            AdjacentNode ptr = head;
            double weight = 0.0;
            while(ptr!=null){
                if(ptr!=head) weight = ptr.getAdjacentLink().getWeight();
                a[i][ptr.getNode().getNodeID()]=weight;
                ptr=ptr.getNextNode();
            }
        }

        boolean[] v = new boolean[node_num]; //방문한 노드
        double[] d = new double[node_num]; //거리

        for(int i=0;i<node_num;i++){ //시작점이 start일 때 거리 그래프 초기화
            d[i]=a[start][i];
        }
        v[start]=true;

        for(int i=0;i<node_num-2;i++){
            int current = 0; //현재 방문 중인 node
            double min=INF;
            for(int j=0;j<node_num;j++){
                if(d[j]<min&&!v[j]){ //방문하지 않고 거리가 가장 짧은 node를 current로 설정
                    min=d[j];
                    current = j;
                }
            }
            v[current]=true; //방문 기록
            for(int j=0;j<node_num;j++){
                if(!v[j]){
                    if(d[current]+a[current][j]<d[j]) { // 거치는 방법이 바로 가는 것보다 최단일 경우
                        d[j]=d[current]+a[current][j]; //최단 거리 갱신
                        path[j] = current;
                    }
                }
            }
        }

        int findroute = path[end];
        ArrayList<Integer> trace = new ArrayList<>();
        trace.add(end);
        while(path[findroute]!=-1){
            trace.add(findroute);
            findroute = path[findroute];
        }
        trace.add(start);
        for(int i=trace.size()-1;i>=0;i--){
            route.add(trace.get(i));
        }
        return route;
    }

    public ArrayList<Integer> astar(RoadNetwork roadNetwork, ArrayList<AdjacentNode> heads, int start, int end){
        ArrayList<Integer> route = new ArrayList<>();
        Node endNode = heads.get(end).getNode();

        int now = start;
        double routeLength = 0.0;

        while(now!=end){
            route.add(now);
            AdjacentNode head = heads.get(now);
            head = head.getNextNode();
            double min = 99999999;
            int nextID = 0;
            double addLength = 0.0;
            while(head!=null){
                double G = routeLength + Calculation.calDistance(head.getNode().getCoordinate(), heads.get(now).getNode().getCoordinate());
                double H = Calculation.calDistance(roadNetwork.getNode(end).getCoordinate(), head.getNode().getCoordinate());
                H = H + 20/ (head.getLink().getWidth());
                /*가중치가 클 수록 큰 길 -> 가중치가 크면 좋음
                 && h가 작을 수록 유리함
                 가중 치가 클 수록 h가 작게 되도록 설정 ==> 거리에 나누기~ (x) + 비중이 너무 커져서 도착지로 도달 x
                                                  ==> 20/가중치 더하기!! + 1/가중치 는 별 효력이 없음
                 */
                double F = G + H;
                if(min > F) {
                    min = F;
                    nextID=head.getNode().getNodeID();
                    addLength = Calculation.calDistance(head.getNode().getCoordinate(), heads.get(now).getNode().getCoordinate());
                }
                head=head.getNextNode();
            }
            routeLength+=addLength;
            now = nextID;
        }
        route.add(end);
        return route;
    }

    public ArrayList<Integer> longest_leg_first(RoadNetwork roadNetwork, ArrayList<AdjacentNode> heads, int start, int end){
        ArrayList<Integer> route = new ArrayList<>();
        int second = 0;
        double max = 0.0;
        double startToend = Calculation.calDistance(heads.get(start).getNode().getCoordinate(),heads.get(end).getNode().getCoordinate());
        double keyToend = startToend;
        AdjacentNode head = heads.get(start);
        while(head.getNextNode()!=null){
            head = head.getNextNode();
            double headToend = Calculation.calDistance(head.getNode().getCoordinate(),heads.get(end).getNode().getCoordinate());
            // back하지 않으면서 가장 긴 link ||혹은~|| 길이가 같다면 end와 더 가까운 쪽
            if((max < head.getLink().getWeight() && headToend<startToend)||max==head.getLink().getWeight()&&keyToend>headToend){
                max = head.getLink().getWeight();
                second = head.getNode().getNodeID();
                keyToend = headToend;
            }
        }
        route.add(start);
        route.addAll(dijkstra(roadNetwork,heads,second,end));
        return route;
    }

    public ArrayList<Integer> fewest_turn(RoadNetwork roadNetwork, ArrayList<AdjacentNode> heads, int start, int end){
        ArrayList<Integer> route = new ArrayList<>();

        int now = start;
        double routeLength = 0.0;
        AdjacentNode prev = null;

        while(now!=end){
            route.add(now);
            AdjacentNode head = heads.get(now);
            head = head.getNextNode();
            double min = 99999999;
            int nextID = 0;
            double addLength = 0.0;
            while(head!=null){
                double G = routeLength + Calculation.calDistance(head.getNode().getCoordinate(), heads.get(now).getNode().getCoordinate());
                double H = Calculation.calDistance(roadNetwork.getNode(end).getCoordinate(), head.getNode().getCoordinate());
                if(prev!=null) {
                    Vector2D prevVec = new Vector2D
                            (prev.getNode().getCoordinate().getX()-heads.get(now).getNode().getCoordinate().getX(),
                                    prev.getNode().getCoordinate().getY()-heads.get(now).getNode().getCoordinate().getY());
                    Vector2D nowVec = new Vector2D
                            (head.getNode().getCoordinate().getX()-heads.get(now).getNode().getCoordinate().getX(),
                                    head.getNode().getCoordinate().getY()-heads.get(now).getNode().getCoordinate().getY() );
                    double angle = prevVec.getAngle(nowVec);
                    H+=3600/angle;
                }
                double F = G + H;
                if(min > F) {
                    min = F;
                    nextID=head.getNode().getNodeID();
                    addLength = Calculation.calDistance(head.getNode().getCoordinate(), heads.get(now).getNode().getCoordinate());
                }
                head=head.getNextNode();
            }
            routeLength+=addLength;
            prev=heads.get(now);
            now = nextID;
        }
        route.add(end);
        return route;
    }
}