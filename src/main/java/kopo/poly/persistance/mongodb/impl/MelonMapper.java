package kopo.poly.persistance.mongodb.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import kopo.poly.dto.MelonDTO;
import kopo.poly.persistance.mongodb.AbstractMongoDBComon;
import kopo.poly.persistance.mongodb.IMelonMapper;
import kopo.poly.util.CmmUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.mongodb.client.model.Updates.set;

@Slf4j
@Component("MelonMapper")
public class MelonMapper extends AbstractMongoDBComon implements IMelonMapper {


	@Override
	public int deleteSong(String pColNm, String singer) throws Exception {
		log.info(this.getClass().getName() + "deleteSong Start!");

		int res = 0;
		MongoCollection<Document> col = mongodb.getCollection(pColNm);

		Document query = new Document();
		query.append("singer", singer);

		FindIterable<Document> rs = col.find(query);

		rs.forEach(e -> {
			col.deleteOne(e);
		});

		res = 1;

		log.info(this.getClass().getName() + ".deleteSong End!");


		return res;
	}

	@Override
	public int updateSongAddListField(String pColNm, String pSinger, List<String> pMember) throws Exception {

		log.info(this.getClass().getName() + ".updateSongAddListField Start!");

		int res = 0;

		MongoCollection<Document> col = mongodb.getCollection(pColNm);
		log.info("pColNm : " + pColNm);
		log.info("pSinger : " + pSinger);


		Document query = new Document();
		query.append("singer", pSinger);

		FindIterable<Document> rs = col.find(query);

		rs.forEach(e -> {
			col.updateOne(e, set("member", pMember));

		});
			res = 1;


		return res;
	}

	@Override
	public int updateSongAddField(String pColNm, String pSinger, String pNickname) throws Exception {
		log.info(this.getClass().getName() + ".updateSongAddField Start!");

		int res = 0;

		MongoCollection<Document> col = mongodb.getCollection(pColNm);

		log.info("pColNm : " + pColNm);
		log.info("pSinger : " + pSinger);


		Document query = new Document();
		query.append("singer", pSinger);


		FindIterable<Document> rs = col.find(query);


		rs.forEach(e -> {
			col.updateOne(e, set("nickname", pNickname));
		});

		res = 1;

		log.info(this.getClass().getName() + "updateSongAddField End!");
		return res;
	}

	@Override
	public int dropMelonCollection(String pColNm) throws Exception {
		log.info(this.getClass().getName() + ".dropMelonCollection Start!");

		int res = 0;

		super.dropCollection(pColNm);

		res = 1;

		log.info(this.getClass().getName() + ".dropMelonCollection End!");

		return res;

	}

	@Override
	public int updateSong(String pColNm, String pSinger, String pUpdateSinger) throws Exception {
		log.info(this.getClass().getName() + ".updateSong Start!");

		int res = 0;

		MongoCollection<Document> col = mongodb.getCollection(pColNm);

		log.info("pColNm : " + pColNm);

		Document query = new Document(); // WHERE
		query.append("singer", pSinger);

		FindIterable<Document> rs = col.find(query); // 조회

		rs.forEach(e -> col.updateOne(e, new Document("$set", new Document("singer", "BTS"))));


		res = 1;

		log.info(this.getClass().getName() + ".updateSong End");


		return res;
	}

	@Override
	public int insertSong(List<MelonDTO> pList, String colNm) throws Exception {

		log.info(this.getClass().getName() + ".insertSong Start!");

		int res = 0;

		if (pList == null) {
			pList = new LinkedList<>();
		}

		// 데이터를 저장할 컬렉션 생성
		super.createCollection(colNm, "collectTime");

		// 저장할 컬렉션 객체 생성
		MongoCollection<Document> col = mongodb.getCollection(colNm);

		for (MelonDTO pDTO : pList) {
			if (pDTO == null) {
				pDTO = new MelonDTO();

			}

			// 레코드 한개씩 저장하기
			col.insertOne(new Document(new ObjectMapper().convertValue(pDTO, Map.class)));

		}

		res = 1;

		log.info(this.getClass().getName() + ".insertSong End!");

		return res;
	}

	@Override
	public List<MelonDTO> getSongList(String colNm) throws Exception {

		log.info(this.getClass().getName() + ".getSongList Start!");

		// 조회 결과를 전달하기 위한 객체 생성하기
		List<MelonDTO> rList = new LinkedList<>();

		MongoCollection<Document> col = mongodb.getCollection(colNm);

		// 조회 결과 중 출력할 컬럼들(SQL의 SELECT절과 FROM절 가운데 컬럼들과 유사함)
		Document projection = new Document();
		projection.append("song", "$song");
		projection.append("singer", "$singer");

		// MongoDB는 무조건 ObjectId가 자동생성되며, ObjectID는 사용하지 않을때, 조회할 필요가 없음
		// ObjectId를 가지고 오지 않을 때 사용함
		projection.append("_id", 0);

		// MongoDB의 find 명령어를 통해 조회할 경우 사용함
		// 조회하는 데이터의 양이 적은 경우, find를 사용하고, 데이터양이 많은 경우 무조건 Aggregate 사용한다.
		FindIterable<Document> rs = col.find(new Document()).projection(projection);

		for (Document doc : rs) {
			if (doc == null) {
				doc = new Document();

			}

			// 조회 잘되나 출력해 봄
			String song = CmmUtil.nvl(doc.getString("song"));
			String singer = CmmUtil.nvl(doc.getString("singer"));

			log.info("song : " + song);
			log.info("singer : " + singer);

			MelonDTO rDTO = new MelonDTO();

			rDTO.setSong(song);
			rDTO.setSinger(singer);

			// 레코드 결과를 List에 저장하기
			rList.add(rDTO);

		}
		log.info(this.getClass().getName() + ".getSongList End!");

		return rList;
	}

	@Override
	public List<Map<String, Object>> getSingerSongCnt(String colNm) throws Exception {

		log.info(this.getClass().getName() + ".getSingerSongCnt Start!");

		// 조회 결과를 전달하기 위한 객체 생성하기
		List<Map<String, Object>> rList = new LinkedList<Map<String, Object>>();

		// MongoDB 조회 쿼리
		List<? extends Bson> pipeline = Arrays.asList(
				new Document().append("$group",
						new Document().append("_id", new Document().append("singer", "$singer")).append("COUNT(singer)",
								new Document().append("$sum", 1))),
				new Document()
						.append("$project",
								new Document().append("singer", "$_id.singer").append("singerCnt", "$COUNT(singer)")
										.append("_id", 0)),
				new Document().append("$sort", new Document().append("singerCnt", 1)));

		MongoCollection<Document> col = mongodb.getCollection(colNm);
		AggregateIterable<Document> rs = col.aggregate(pipeline).allowDiskUse(true);

		for (Document doc : rs) {

			if (doc == null) {
				doc = new Document();
			}

			String singer = doc.getString("singer");
			int singerCnt = doc.getInteger("singerCnt", 0);

			log.info("singer : " + singer);
			log.info("singerCnt : " + singerCnt);

			Map<String, Object> rMap = new LinkedHashMap<String, Object>();

			rMap.put("singer", singer);
			rMap.put("singerCnt", singerCnt);

			rList.add(rMap);

			rMap = null;
			doc = null;
		}

		Iterator<Document> cursor = null;
		rs = null;
		col = null;
		pipeline = null;

		log.info(this.getClass().getName() + ".getSingerSongCnt End!");

		return rList;
	}

	@Override
	public List<MelonDTO> getSingerSong(String pColNm, String pSinger) throws Exception {
		log.info(this.getClass().getName() + ".getSingerSong Start!");

		List<MelonDTO> rList = new LinkedList<>();

		MongoCollection<Document> col = mongodb.getCollection(pColNm);

		Document query = new Document();
		query.append("singer", pSinger);

		Document projection = new Document();
		projection.append("song", "$song");
		projection.append("singer", "$singer");

		projection.append("_id", 0);


		FindIterable<Document> rs = col.find(query).projection(projection);

		for (Document doc : rs) {
			if (doc == null) {
				doc = new Document();
			}

			String song = CmmUtil.nvl(doc.getString("song"));
			String singer = CmmUtil.nvl(doc.getString("singer"));


			log.info("song : " + song);
			log.info("singer : " + singer);

			MelonDTO rDTO = new MelonDTO();

			rDTO.setSinger(singer);
			rDTO.setSong(song);


			rList.add(rDTO);
		}

		log.info(this.getClass().getName() + ".getSingerSong End!");
		return rList;
	}

	@Override
	public int insertMany(List<MelonDTO> pList, String colNm) throws Exception {
		log.info(this.getClass().getName() + ".insertMany Start!");


		int res = 0;

		if (pList == null) {
			pList = new LinkedList<>();
		}

		// 데이터를 저장할 컬렉션 생성
		super.createCollection(colNm, "collectTime");
		MongoCollection<Document> col = mongodb.getCollection(colNm);

		List<Document> rList = new ArrayList<>();

		pList.stream().forEach(melon ->{
			rList.add(new Document(new ObjectMapper().convertValue(melon, Map.class)));
			// new ModelMapper().map(melon, Map.class);
		});

		col.insertMany(rList);

		res = 1;

		log.info(this.getClass().getName() + ".insertSong End!");
		return res;
	}
}
