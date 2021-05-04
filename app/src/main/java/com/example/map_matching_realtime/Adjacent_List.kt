package com.example.map_matching_realtime

import android.graphics.Color
import android.util.Pair
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons

class Adjacent_List(naverMap: NaverMap) {
    private lateinit var naverMap: NaverMap

    fun Adjacent_List(naverMap: NaverMap, dir: String): RoadNetwork? {
        val testNo = 1 // 여기만 바꿔주면 됨 (PilotTest 2는 data 1만 존재)

        val fileIO = FileIO(dir) // 파일에서 읽어와 도로네트워크 생성
        val roadNetwork = fileIO.generateRoadNetwork()

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
        //도로네트워크 생성

        //getNodePrint(roadNetwork, naverMap) //노드 출력
        return roadNetwork
    }

    //Node(좌표)를 지도위에 출력하는 함수
    fun getNodePrint(roadNetwork: RoadNetwork, naverMap: NaverMap) {
        for (i in roadNetwork.nodeArrayList.indices) { //indices 또는 index사용
            val marker = Marker() //좌표
            marker.position = LatLng(
                roadNetwork.getNode(i).coordinate.y,
                roadNetwork.getNode(i).coordinate.x
            ) //node 좌표 출력
            marker.icon = MarkerIcons.BLACK //색을 선명하게 하기 위해 해줌
            marker.iconTintColor = Color.BLUE //색 덧입히기
            marker.width = 30
            marker.height = 50
            // 마커가 너무 커서 크기 지정해줌
            marker.map = naverMap //navermap에 출력
        } //모든 노드 출력

        var cameraUpdate = CameraUpdate.scrollAndZoomTo(
            LatLng(
                roadNetwork.getNode(0).coordinate.x, roadNetwork.getNode(0).coordinate.y
            ),18.0
        )
        naverMap.moveCamera(cameraUpdate)
        //카메라 이동
    }
}