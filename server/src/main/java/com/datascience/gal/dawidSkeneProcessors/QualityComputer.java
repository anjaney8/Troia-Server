package com.datascience.gal.dawidSkeneProcessors;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;

import com.datascience.gal.Category;
import com.datascience.gal.Datum;
import com.datascience.gal.DawidSkene;
import com.datascience.gal.quality.ClassificationCostEvaluator;
import com.datascience.gal.quality.EvaluatorManager;
import com.datascience.gal.service.DawidSkeneCache;

public class QualityComputer extends DawidSkeneProcessor {

	private static final String DEFAULT_METHOD = "MV_MaxLikelihoodEvaluator";
	
	protected QualityComputer(String id, DawidSkeneCache cache) {
		this(id,cache,DEFAULT_METHOD);
	}
	
	protected QualityComputer(String id, DawidSkeneCache cache, String method) {
		super(id, cache);
		this.evaluator = EvaluatorManager.getEvaluatorForMethod(method);
	}

	@Override
	public void run() {
		DawidSkene ds = null;
		try {
			logger.info("Executing quality computer for "
					+ this.getDawidSkeneId() + ".");
			ds = this.getCache().getDawidSkeneForEditing(
					this.getDawidSkeneId(), this);
			Map<String, Category> categories = ds.getCategories();
			Map<String, Datum> objects = ds.getObjects();
			Collection<String> categoryNames = categories.keySet();
			Collection<String> objectNames = objects.keySet();
			for (String objectName : objectNames) {
				for (String categoryName : categoryNames) {
					ds.computeProjectQuality(this.evaluator, categoryName,
							objectName);
				}
			}
			logger.info("Quality computer for " + this.getDawidSkeneId()
					+ " finished.");
		} catch (Exception e) {
			logger.error("Failed to execute qualityComputer because : "
					+ e.getMessage());
		} finally {
			this.getCache().insertDawidSkene(ds, this);
			this.setState(DawidSkeneProcessorState.FINISHED);
		}
	}

	private ClassificationCostEvaluator evaluator;

	private static Logger logger = Logger.getLogger(QualityComputer.class);
}
