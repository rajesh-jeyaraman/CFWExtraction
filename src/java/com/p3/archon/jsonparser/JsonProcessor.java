package com.p3.archon.jsonparser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.p3.archon.jsonparser.bean.Children;
import com.p3.archon.jsonparser.bean.FileModel;
import com.p3.archon.jsonparser.bean.FinalChildren;
import com.p3.archon.jsonparser.bean.FinalResult;


public class JsonProcessor {
	public static List<FinalChildren> resultList = new ArrayList<>();

	public static void readJsonWithObjectMapper(String fileName) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		FileModel result = objectMapper.readValue(new File(fileName), FileModel.class);

		List<Children> nodeList = result.getResult().getChildren();

		getFinalJson("/" + result.getResult().getName(), nodeList);

		FinalResult fr = new FinalResult();
		fr.setResult(resultList);
		File file = new File("FinalResult.json");
		file.createNewFile();
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(new Gson().toJson(fr));
		fileWriter.flush();
		fileWriter.close();

	}

	private static void getFinalJson(String name, List<Children> nodeList) {
		for (Children nodes : nodeList) {
			FinalChildren mNode = new FinalChildren();
			mNode.setTopath(name + nodes.getFrompath().substring(nodes.getFrompath().lastIndexOf("/"),
					nodes.getFrompath().length()));

			mNode.setFrompath(nodes.getFrompath());
			mNode.setDatatype(nodes.getDatatype());
			mNode.setName(nodes.getName());
			mNode.setSearch(nodes.isSearch());
			mNode.setResult(nodes.isResult());
			mNode.setId(nodes.getId());
			mNode.setFilename(nodes.getFilename());
			mNode.setMinoccurance(nodes.getMinoccurance());
			mNode.setMaxoccurance(nodes.getMaxoccurance());

			if (nodes.getChildren().size() > 0) {
				getFinalJson(name + "/" + nodes.getName(), nodes.getChildren());
			} else {
				resultList.add(mNode);
			}
		}

	}
}
