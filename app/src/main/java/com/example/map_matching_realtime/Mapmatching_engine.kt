package com.example.map_matching_realtime

import android.graphics.Color
import android.util.Pair
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons

class Mapmatching_engine(naverMap: NaverMap) {

    private lateinit var naverMap: NaverMap
    private val emission: Emission = Emission()
    private val transition:Transition = Transition()
    private val wSize = 3 //윈도사이즈는 3!!!!!!

    fun engine(naverMap: NaverMap, dir: String){

        System.out.println("===== [YSY] Map-matching real time =====")

        val testNo = 1 // 여기만 바꿔주면 됨 (PilotTest 2는 data 1만 존재)

        val fileIO = FileIO(dir) // 파일에서 읽어와 도로네트워크 생성
        val roadNetwork = fileIO.generateRoadNetwork()

        //yh_tp
        ///////////// Transition probability matrix 구하기 (yh_tp)////////////////
        val n = roadNetwork.linksSize
        val tp_matrix = Array(n) {
            DoubleArray(n)
        }
        for (i in 0 until n) {
            // 여기에서 link[i]가 몇개의 link와 맞닿아있는지 int 변수 선언해서 저장
            val m = roadNetwork.getLink(i).nextLinksNum(roadNetwork)
            // 알고리즘대로 tp 지정
            for (j in 0 until n) {
                if (i == j) tp_matrix[i][j] = 0.5 else if (roadNetwork.getLink(i)
                        .isLinkNextTo(roadNetwork, j)
                ) tp_matrix[i][j] = 1.0 / m else tp_matrix[i][j] = 0.0
            }
        }
        //yh_tp 끝

        // Link와 Node를 바탕으로 Adjacent List 구축
        val heads: ArrayList<AdjacentNode> = ArrayList()
        for (i in roadNetwork.nodeArrayList.indices) {
            val headNode = AdjacentNode(roadNetwork.nodeArrayList[i])
            heads.add(headNode)
            val adjacentLink: MutableList<Pair<Link, Int>>? =
                roadNetwork.getLink1(headNode.node.nodeID) //mutableList?
            if (adjacentLink != null) { //안전하게 하기 위함
                if (adjacentLink.size == 0) continue
            }
            var ptr = headNode
            if (adjacentLink != null) { //안전하게 하기 위함
                for (j in adjacentLink.indices) {
                    val addNode = AdjacentNode(
                        roadNetwork.getNode(adjacentLink[j].second), adjacentLink[j].first
                    )
                    ptr.nextNode = addNode
                    ptr = ptr.nextNode
                }
            }
        }
        //신기한 사실 = get,set 함수를 불러오지 않아도 알아서 척척박사님 알아맞춰보세요
        //여기까지 도로네트워크 생성

        // GPS points와 routePoints를 저장할 ArrayList생성
        val gpsPointArrayList: ArrayList<GPSPoint> = ArrayList()
        val routePointArrayList: ArrayList<Point> // 실제 경로의 points!

        val matchingCandiArrayList: ArrayList<Candidate> = ArrayList()

        // test 번호에 맞는 routePoints생성
        routePointArrayList = roadNetwork.routePoints(testNo)

        // window size만큼의 t-window, ... , t-1, t에서의 candidates의 arrayList
        val arrOfCandidates: ArrayList<ArrayList<Candidate>> = ArrayList()
        val subGPSs: ArrayList<GPSPoint> = ArrayList()

        var timestamp = 0

        // 1: 원래 하던대로 (표준편차 4)  | 2: x혹은 y좌표만 uniform하게(hor, ver, dia에 따라서)
        // 3: x, y 모두 uniform하게     | 4: 교수님이 말한 평균 4 방식
        val gpsGenMode = 2
        println("Fixed Sliding Window Viterbi (window size: 3)")
        for (i in routePointArrayList.indices step (1)) {
            var point: Point = routePointArrayList.get(i)
            println("routePoint: " + point)
            printPoint(point, Color.YELLOW, naverMap)
        }

        ////////////////////반복문 - gps 생성////////////////////////////////
        for (i in routePointArrayList.indices step (1)) {
            // 오래 걸리는 작업 수행부분
            var point: Point = routePointArrayList.get(i)
            val gpsPoint = GPSPoint(
                timestamp,
                point,
                gpsGenMode,
                3,
                roadNetwork.getLink(point.linkID).itLooksLike
            )
            printPoint(gpsPoint.point, Color.RED, naverMap) // 생성된 GPS출력(빨간색)
            println("[MAIN] GPS: $gpsPoint")
            gpsPointArrayList.add(gpsPoint)
            timestamp++ //gps 생성후 바로 증가

            val candidates: ArrayList<Candidate> = ArrayList()
            candidates.addAll(
                Candidate.findRadiusCandidate(
                    gpsPointArrayList, matchingCandiArrayList,
                    gpsPoint.point, 50, roadNetwork, timestamp, emission, transition
                )
            )

            //candidate출력 주석
            /*
            println(">>>> [MAIN] candidates <<<<")
            for (candidate in candidates) {
                println("  $candidate")
            }
            println(">>>>>>>>>>>>>><<<<<<<<<<<<<")
             */

            ///////////// FSW VITERBI /////////////
            subGPSs.add(gpsPoint)
            arrOfCandidates.add(candidates)

            ///////////////////matching 진행하는 부분분//////////////////
            //처음 부분 3번은 제일 가까운 candidate에 매칭 (ep)
            if(timestamp <=3){
                // 마지막 candidates 중 prob가 가장 높은 것 max_last_candi에 저장
                var max_last_candi: Candidate? = Candidate()
                var max_prob = 0.0
                for (candidate in candidates) {
                    if (max_prob < candidate.ep) {
                        max_prob = candidate.ep
                        max_last_candi = candidate
                    }
                }
                FSWViterbi.setMatched_sjtp(max_last_candi) //가장 ep가 높은 candidate 매칭

                Emission.Emission_Median(FSWViterbi.getMatched_sjtp().get(timestamp-1))
                //매칭된 candidate의 median값 저장

                if(timestamp!=1){ //matching 두 개 이상일때부터 median 저장가능
                    var tp = 0.0;
                    tp = Transition.Transition_pro(subGPSs[timestamp-2].point, subGPSs[timestamp-1].point, FSWViterbi.getMatched_sjtp().get(timestamp-2), FSWViterbi.getMatched_sjtp().get(timestamp-1), roadNetwork)
                    FSWViterbi.getMatched_sjtp().get(timestamp-2).setTp(tp)

                    Transition.Transition_Median(FSWViterbi.getMatched_sjtp().get(timestamp-2)) //매칭된 candidate와 그 전 매칭된 tp의 median값 저장
                    if(timestamp == 3){
                        subGPSs.clear()
                        arrOfCandidates.clear();
                    }
                }

                continue
            }
            //처음 3번 구하는 부분 끝, 처음 매칭 3번은 비터비 적용x

            if (subGPSs.size == wSize){
                println("===== VITERBI start ====")

                println("----- sjtp ------")
                FSWViterbi.generateMatched(
                    tp_matrix,
                    wSize,
                    arrOfCandidates,
                    gpsPointArrayList,
                    transition,
                    timestamp,
                    roadNetwork,
                    "sj",
                )
                subGPSs.clear()
                arrOfCandidates.clear()
                //clear
                subGPSs.add(gpsPoint)
                arrOfCandidates.add(candidates)
                //마지막 gps, candidate 추가

                println("===== VITERBI end ====")
            }
            //비터비 끝//
        }
        printMatched(FSWViterbi.getMatched_sjtp(), Color.GREEN, 50, naverMap) // 세정 매칭: 초록색

    }

    //point 출력하는 함수
    fun printPoint(point: Point, COLOR: Int, naverMap: NaverMap) {
        val marker = Marker() //좌표
        marker.position = LatLng(
            point.y,
            point.x
        )
        marker.icon = MarkerIcons.BLACK //색을 선명하게 하기 위해 해줌
        marker.iconTintColor = COLOR //색 덧입히기
        marker.width = 30
        marker.height = 30
        // 마커가 너무 커서 크기 지정해줌
        marker.map = naverMap //navermap에 출력
        var cameraUpdate = CameraUpdate.scrollAndZoomTo(
            LatLng(
                point.y,
                point.x
            ), 17.0
        )
        naverMap.moveCamera(cameraUpdate)
        //카메라 이동
    }

    fun printMatched(matched: ArrayList<Candidate>, COLOR: Int, SIZE: Int, naverMap: NaverMap) {
        for (i in matched.indices) { //indices 또는 index사용
            val marker = Marker() //좌표
            marker.position = LatLng(
                matched.get(i).point.y,
                matched.get(i).point.x
            ) //node 좌표 출력
            marker.icon = MarkerIcons.BLACK //색을 선명하게 하기 위해 해줌
            marker.iconTintColor = COLOR //색 덧입히기
            marker.width = SIZE
            marker.height = SIZE
            // 마커가 너무 커서 크기 지정해줌
            marker.map = naverMap //navermap에 출력
        } //모든 노드 출력

        var cameraUpdate = CameraUpdate.scrollAndZoomTo(
            LatLng(
                matched.get(0).point.y,
                matched.get(0).point.x
            ), 18.0
        )
        naverMap.moveCamera(cameraUpdate)

        //카메라 이동
    }

}