<div class="page-header">
			<h3>Macierz pełnomocnictw</h3>
</div>


<div class="panel-group" id="accordion">
		  <div class="panel panel-default">
			<div class="panel-heading">
			  <h4 class="panel-title">
				<a data-toggle="collapse" data-parent="#accordion" href="#collapseOne">
				  Wyszukiwarka pełnomocnictw
				</a>
			  </h4>
			</div>
			<div id="collapseOne" class="panel-collapse collapse in">
			  <div class="panel-body">
			       <%@include file="search_form.jsp" %>
			  </div>
			</div>
		  </div>
		  <div class="panel panel-default">
			<div class="panel-heading">
			  <h4 class="panel-title">
				<a data-toggle="collapse" data-parent="#accordion" href="#collapseTwo">
				  Wyniki wyszukiwania
				</a>
			  </h4>
			</div>
			<div id="collapseTwo" class="panel-collapse collapse">
			  <div class="panel-body">
				    <%@include file="result_table.jsp" %>
			  </div>
			</div>
		  </div>
		</div>


	</div>
    <%@include file="category_modal.jsp" %>




	<script>
		var category_tree;
		var selected_ids="";
		$(document).ready(function(){
			$('#apw_category_button').click(function(){

			});
			$('#categoryModal').on('hidden.bs.modal', function (e) {
			  // do something...
			})
			$('#categoryModal').on('show.bs.modal', function (e) {
				if(!category_tree){
					initCategoryTree();
				}
			})
			$('#categoryModal button.btn-primary').click(function(){
				selected_ids = $('#category_tree_div').jstree(true).get_selected();
				$("#category_info").html(selected_ids.length);
				$('#categoryModal').modal('hide')
			});
		});
		function initCategoryTree(){
			    category_tree = $('#category_tree_div').jstree({
                        "checkbox" : {
                          "keep_selected_style" : false
                        },
                        "plugins" : [ "checkbox",  "sort",  "state", "types", "wholerow" ],
                        'core' : {
                            'data' : {
                                    'url' : function (node) {
                                      return node.id === '#' ?
                                        dispatcherPortlet+'&controller=dpdservice&action=getRootCat' :
                                        dispatcherPortlet+'&controller=dpdservice&action=getChildCat';
                                    },
                                    'data' : function (node) {
                                      return { 'id' : node.id };
                                    }
                            }
                        }
                    });

				$('#category_tree_div').on("changed.jstree", function (e, data) {
					/*if(data.action == "deselect_node"){

					}else if(data.action ==  "select_node"){

					}*/

				});
		}
	</script>