package com.development.commons.tools.xi.util.resource;

/**
 * Visitor that can scan all resources produced by a ResourceScanner.
 *
 * @author ddiodati
 * @see ResourceScanner
 */
public interface ResourceVisitor<T> {
  /**
   * Indicates if scanning should stop. Can be used to stop the ResourceScanner from scanning any more once needed
   * resources have been found. This is called before each call to visit.
   *
   * @return true will cause the ResourceScanner to stop executing. false will allow the ResourceScanner to continue
   *         scanning.
   */
  boolean isDone();

  /**
   * Return an array of supported file extensions to filter on. This supports the wild card: &#42; for example,
   * {"&#42;.gif","&#42;.jpg"}, or {"&#42;"}
   *
   * This is a second level filtering since the ResourceScanner also has a filtering pattern( This can be implemented in
   * base vistors that other ones could use. For example {@link ClassVisitor}.
   *
   * @return The extensions to look for.
   */
  String[] getSupportedFileExts();

  /**
   * This method gets called with each resource found.
   *
   * @param obj
   *          The specific resource object.
   */
  void visit(T obj);

  /**
   * This method is called just before the visit method to handle converting a resource into the given type. This can be
   * implemented in base vistors that other ones could use. For example {@link ClassVisitor}.
   *
   * @param resource
   *          The resource object to convert
   * @return The needed type of object to invoke visit with.
   *
   *         NOTE if null is returned then this resource will be skipped and the visit method will not be invoked.
   *
   */
  T convertObject(Resource resource);

}