package com.p3.archon.jsonparser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.p3.archon.jsonparser.bean.FileModel;
import com.p3.archon.jsonparser.bean.FinalNodes;
import com.p3.archon.jsonparser.bean.FinalResult;
import com.p3.archon.jsonparser.bean.Nodes;

public class JsonProcessor {

	public static void readJsonWithObjectMapper(String fileName) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		FileModel result = null;
		try {
			result = objectMapper.readValue(new File(fileName), FileModel.class);
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<Nodes> nodeList = result.getResult().getNodes();
		List<FinalNodes> resultList = new ArrayList<>();

		getFinalJson("/" + result.getResult().getName(), resultList, nodeList);

		FinalResult fr = new FinalResult();
		fr.setResult(resultList);
		File file = new File("FinalJson.json");
		file.createNewFile();
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(new Gson().toJson(fr));
		fileWriter.flush();
		fileWriter.close();

	}

	private static void getFinalJson(String name, List<FinalNodes> resultList, List<Nodes> nodeList) {
		FinalNodes mNode = new FinalNodes();
		for (Nodes nodes : nodeList) {
			mNode.setTopath(name + nodes.getFrompath().substring(nodes.getFrompath().lastIndexOf("/"),
					nodes.getFrompath().length()));

			mNode.setFrompath("/" + nodes.getFrompath());
			mNode.setDatatype(nodes.getDatatype());
			mNode.setName(nodes.getName());
			mNode.setSearch(nodes.isSearch());
			mNode.setResult(nodes.isResult());
			mNode.setCondition(nodes.getCondition());
			mNode.setId(nodes.getId());
			mNode.setFilename(nodes.getFilename());
			resultList.add(mNode);
			if (nodes.getNodes().size() > 0) {
				getFinalJson(name + "/" + nodes.getName(), resultList, nodes.getNodes());
			}
		}

	}

}
