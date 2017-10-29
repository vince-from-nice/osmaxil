package org.openstreetmap.osmaxil.model.misc;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmaxil.model.ElementType;

public class ElementIdWithParentFlags {

	Long osmId;

	ElementType type;

	List<Parent> parents = new ArrayList<>();

	public class Parent {

		private Long osmId;

		private Boolean flag;

		public Long getOsmId() {
			return osmId;
		}

		public void setOsmId(Long osmId) {
			this.osmId = osmId;
		}

		public Boolean getFlag() {
			return flag;
		}

		public void setFlag(Boolean flag) {
			this.flag = flag;
		}
	}

	public Long getOsmId() {
		return osmId;
	}

	public void setOsmId(Long osmId) {
		this.osmId = osmId;
	}

	public List<Parent> getParents() {
		return parents;
	}

	public void setParents(List<Parent> parents) {
		this.parents = parents;
	}

	public ElementType getType() {
		return type;
	}

	public void setType(ElementType type) {
		this.type = type;
	}

}
