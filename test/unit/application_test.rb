require "test_helper"

class ApplicationTest < Test::Unit::TestCase
  def test_register_will_register_application_with_given_client
    router_applications = stub('applications')
    router_client = stub('router client', applications: router_applications)
    router_applications.expects(:update).with(application_id: "id", backend_url: "http://backend.url")
    app = Router::Application.new(router_client, "id", "http://backend.url")
    app.register!
  end

  def test_ensure_prefix_route_will_register_a_prefix_route_using_the_client
    router_routes = stub('routes')
    router_client = stub('router client', routes: router_routes)
    router_routes.expects(:update).with(application_id: "id", route_type: :prefix, incoming_path: "/path")
    app = Router::Application.new(router_client, "id", "http://backend.url")
    app.ensure_prefix_route("/path")
  end

  def test_ensure_route_will_register_a_full_route_using_the_client
    router_routes = stub('routes')
    router_client = stub('router client', routes: router_routes)
    router_routes.expects(:update).with(application_id: "id", route_type: :full, incoming_path: "/path")
    app = Router::Application.new(router_client, "id", "http://backend.url")
    app.ensure_full_route("/path")
  end
end