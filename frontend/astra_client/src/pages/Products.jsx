import { useEffect, useState } from "react";
import {
  Container,
  Grid,
  Typography,
  Pagination,
  Stack,
  CircularProgress,
  Box,
  Paper,
  Divider,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Button,
  Chip,
  Alert,
  Drawer,
  IconButton,
  useMediaQuery,
  useTheme
} from "@mui/material";

import {
  FilterList,
  SearchOff,
  Close
} from "@mui/icons-material";

import { useSearchParams } from "react-router-dom";

import {
  productApi,
  searchApi,
  categoryApi
} from "../api/api";

import ProductCard from "../components/ProductCard";


export default function Products() {
  const [params, setParams] = useSearchParams();

  const q = params.get("q") || "";
  const categoryId =
    params.get("categoryId") || "";

  const pageFromUrl =
    Number(params.get("page") || 0);

  const theme = useTheme();

  const isMobile =
    useMediaQuery(
      theme.breakpoints.down("md")
    );


  const [data, setData] = useState({
    products: [],
    totalPages: 1,
    page: 0
  });

  const [categories, setCategories] =
    useState([]);

  const [loading, setLoading] =
    useState(true);

  const [categoryLoading, setCategoryLoading] =
    useState(true);

  const [error, setError] =
    useState("");

  const [selectedCategory, setSelectedCategory] =
    useState(categoryId);

  const [sort, setSort] =
    useState("default");

  const [mobileFilters, setMobileFilters] =
    useState(false);


  /*
   * Keep selected category synchronized
   * with the URL.
   */
  useEffect(() => {
    setSelectedCategory(categoryId);
  }, [categoryId]);


  /*
   * Load categories.
   */
  useEffect(() => {
    let cancelled = false;

    const loadCategories = async () => {
      try {
        const response =
          await categoryApi.list();

        if (cancelled) {
          return;
        }

        const result =
          response.data;

        setCategories(
          result?.categories ||
          result?.content ||
          result ||
          []
        );
      } catch (err) {
        if (!cancelled) {
          console.error(
            "Failed to load categories",
            err
          );
        }
      } finally {
        if (!cancelled) {
          setCategoryLoading(false);
        }
      }
    };

    loadCategories();

    return () => {
      cancelled = true;
    };
  }, []);


  /*
   * API-only product loader.
   *
   * IMPORTANT:
   * This function does not call setState().
   * That makes it safe to use from effects.
   */
  const fetchProducts = async (
    requestedPage,
    selectedCat
  ) => {
    const searchParams = {
      q: q || undefined,
      categoryId:
        selectedCat || undefined,
      page: requestedPage,
      size: 12
    };

    const response =
      q || selectedCat
        ? await searchApi.products(
            searchParams
          )
        : await productApi.list({
            page: requestedPage,
            size: 12
          });

    const result =
      response.data;

    const products =
      result?.products ||
      result?.content ||
      result ||
      [];

    return {
      products: Array.isArray(products)
        ? products
        : [],

      totalPages:
        Number(
          result?.totalPages || 1
        ),

      page:
        Number(
          result?.page ??
          requestedPage
        )
    };
  };


  /*
   * Load products whenever the URL
   * search/category/page changes.
   *
   * All setState calls happen after the
   * asynchronous API operation.
   */
  useEffect(() => {
    let cancelled = false;

    const loadProducts = async () => {
      setLoading(true);
      setError("");

      try {
        const result =
          await fetchProducts(
            pageFromUrl,
            categoryId
          );

        if (cancelled) {
          return;
        }

        setData(result);
      } catch (err) {
        if (cancelled) {
          return;
        }

        console.error(
          "Failed to load products",
          err
        );

        setError(
          err?.response?.data?.message ||
          "Unable to load products. Please try again."
        );

        setData({
          products: [],
          totalPages: 1,
          page: 0
        });
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    loadProducts();

    return () => {
      cancelled = true;
    };
  }, [
    q,
    categoryId,
    pageFromUrl
  ]);


  /*
   * Category selection.
   */
  const handleCategoryChange = (
    value
  ) => {
    setSelectedCategory(value);

    const nextParams =
      new URLSearchParams(params);

    if (value) {
      nextParams.set(
        "categoryId",
        value
      );
    } else {
      nextParams.delete(
        "categoryId"
      );
    }

    nextParams.delete("page");

    setParams(nextParams);
  };


  /*
   * Pagination.
   */
  const handlePageChange = (
    _event,
    value
  ) => {
    const nextPage =
      value - 1;

    const nextParams =
      new URLSearchParams(params);

    if (nextPage > 0) {
      nextParams.set(
        "page",
        String(nextPage)
      );
    } else {
      nextParams.delete("page");
    }

    setParams(nextParams);

    window.scrollTo({
      top: 0,
      behavior: "smooth"
    });
  };


  /*
   * Clear filters.
   */
  const clearFilters = () => {
    setSelectedCategory("");

    const nextParams =
      new URLSearchParams();

    if (q) {
      nextParams.set("q", q);
    }

    setParams(nextParams);

    setMobileFilters(false);
  };


  /*
   * Client-side sorting.
   */
  const sortedProducts =
    [...data.products].sort(
      (a, b) => {
        if (
          sort === "price-low"
        ) {
          return (
            Number(
              a.discountPrice ??
              a.price ??
              0
            ) -
            Number(
              b.discountPrice ??
              b.price ??
              0
            )
          );
        }

        if (
          sort === "price-high"
        ) {
          return (
            Number(
              b.discountPrice ??
              b.price ??
              0
            ) -
            Number(
              a.discountPrice ??
              a.price ??
              0
            )
          );
        }

        if (
          sort === "rating"
        ) {
          return (
            Number(
              b.rating ?? 0
            ) -
            Number(
              a.rating ?? 0
            )
          );
        }

        return 0;
      }
    );


  const filterContent = (
    <Box
      sx={{
        p: isMobile ? 2 : 0
      }}
    >
      {isMobile && (
        <Stack
          direction="row"
          alignItems="center"
          justifyContent="space-between"
          mb={2}
        >
          <Typography
            variant="h6"
            fontWeight={700}
          >
            Filters
          </Typography>

          <IconButton
            onClick={() =>
              setMobileFilters(false)
            }
          >
            <Close />
          </IconButton>
        </Stack>
      )}

      <Typography
        variant="subtitle1"
        fontWeight={700}
        mb={1.5}
      >
        Categories
      </Typography>

      <FormControl
        fullWidth
        size="small"
      >
        <InputLabel>
          Category
        </InputLabel>

        <Select
          value={selectedCategory}
          label="Category"
          onChange={(e) =>
            handleCategoryChange(
              e.target.value
            )
          }
          disabled={
            categoryLoading
          }
        >
          <MenuItem value="">
            All Categories
          </MenuItem>

          {categories.map(
            (category) => (
              <MenuItem
                key={category.id}
                value={category.id}
              >
                {category.name}
              </MenuItem>
            )
          )}
        </Select>
      </FormControl>

      <Divider sx={{ my: 3 }} />

      <Typography
        variant="subtitle1"
        fontWeight={700}
        mb={1.5}
      >
        Search
      </Typography>

      <TextField
        fullWidth
        size="small"
        value={q}
        placeholder="Search products..."
        InputProps={{
          readOnly: true
        }}
      />

      <Divider sx={{ my: 3 }} />

      <Button
        fullWidth
        variant="outlined"
        onClick={clearFilters}
      >
        Clear Filters
      </Button>

      {isMobile && (
        <Button
          fullWidth
          variant="contained"
          sx={{ mt: 1 }}
          onClick={() =>
            setMobileFilters(false)
          }
        >
          Apply
        </Button>
      )}
    </Box>
  );


  return (
    <Container
      maxWidth="xl"
      sx={{
        py: {
          xs: 3,
          md: 5
        }
      }}
    >
      {/* Header */}
      <Stack
        direction={{
          xs: "column",
          sm: "row"
        }}
        justifyContent="space-between"
        alignItems={{
          xs: "flex-start",
          sm: "center"
        }}
        spacing={2}
        mb={3}
      >
        <Box>
          <Typography
            variant="h4"
            fontWeight={800}
            sx={{
              fontSize: {
                xs: "1.8rem",
                md: "2.2rem"
              }
            }}
          >
            {q
              ? `Search results for "${q}"`
              : "All Products"}
          </Typography>

          {!loading && (
            <Typography
              variant="body2"
              color="text.secondary"
              sx={{ mt: 0.5 }}
            >
              {data.products.length}{" "}
              products found
            </Typography>
          )}
        </Box>

        <Stack
          direction="row"
          spacing={1}
          alignItems="center"
          width={{
            xs: "100%",
            sm: "auto"
          }}
        >
          {isMobile && (
            <Button
              variant="outlined"
              startIcon={<FilterList />}
              onClick={() =>
                setMobileFilters(true)
              }
              sx={{ flex: 1 }}
            >
              Filters
            </Button>
          )}

          <FormControl
            size="small"
            sx={{
              minWidth: 180
            }}
          >
            <InputLabel>
              Sort By
            </InputLabel>

            <Select
              value={sort}
              label="Sort By"
              onChange={(e) =>
                setSort(
                  e.target.value
                )
              }
            >
              <MenuItem value="default">
                Recommended
              </MenuItem>

              <MenuItem value="price-low">
                Price: Low to High
              </MenuItem>

              <MenuItem value="price-high">
                Price: High to Low
              </MenuItem>

              <MenuItem value="rating">
                Highest Rated
              </MenuItem>
            </Select>
          </FormControl>
        </Stack>
      </Stack>


      {/* Active filters */}
      {(q || categoryId) && (
        <Stack
          direction="row"
          spacing={1}
          flexWrap="wrap"
          useFlexGap
          mb={3}
        >
          {q && (
            <Chip
              label={`Search: ${q}`}
              onDelete={() => {
                const nextParams =
                  new URLSearchParams(
                    params
                  );

                nextParams.delete(
                  "q"
                );

                nextParams.delete(
                  "page"
                );

                setParams(
                  nextParams
                );
              }}
            />
          )}

          {categoryId && (
            <Chip
              label="Category selected"
              onDelete={() =>
                handleCategoryChange(
                  ""
                )
              }
            />
          )}
        </Stack>
      )}


      {/* Error */}
      {error && (
        <Alert
          severity="error"
          sx={{ mb: 3 }}
        >
          {error}
        </Alert>
      )}


      <Grid
        container
        spacing={3}
      >

        {/* Desktop filters */}
        {!isMobile && (
          <Grid
            size={{
              xs: 12,
              md: 3,
              lg: 2.5
            }}
          >
            <Paper
              elevation={0}
              sx={{
                p: 2.5,
                border: "1px solid",
                borderColor:
                  "divider",
                borderRadius: 2,
                position:
                  "sticky",
                top: 90
              }}
            >
              {filterContent}
            </Paper>
          </Grid>
        )}


        {/* Products */}
        <Grid
          size={{
            xs: 12,
            md: 9,
            lg: 9.5
          }}
        >
          {loading ? (
            <Box
              sx={{
                minHeight: 400,
                display: "flex",
                alignItems:
                  "center",
                justifyContent:
                  "center"
              }}
            >
              <Stack
                spacing={2}
                alignItems="center"
              >
                <CircularProgress />

                <Typography
                  color="text.secondary"
                  variant="body2"
                >
                  Loading products...
                </Typography>
              </Stack>
            </Box>
          ) : sortedProducts.length === 0 ? (
            <Paper
              elevation={0}
              sx={{
                minHeight: 400,
                display: "flex",
                alignItems:
                  "center",
                justifyContent:
                  "center",
                textAlign: "center",
                p: 4,
                border: "1px solid",
                borderColor:
                  "divider",
                borderRadius: 2
              }}
            >
              <Stack
                spacing={2}
                alignItems="center"
              >
                <SearchOff
                  sx={{
                    fontSize: 70,
                    color:
                      "text.disabled"
                  }}
                />

                <Typography
                  variant="h6"
                  fontWeight={700}
                >
                  No products found
                </Typography>

                <Typography
                  color="text.secondary"
                  maxWidth={450}
                >
                  We couldn't find any
                  products matching
                  your search or selected
                  filters.
                </Typography>

                <Button
                  variant="contained"
                  onClick={
                    clearFilters
                  }
                >
                  View All Products
                </Button>
              </Stack>
            </Paper>
          ) : (
            <>
              <Grid
                container
                spacing={3}
              >
                {sortedProducts.map(
                  (product) => (
                    <Grid
                      key={product.id}
                      size={{
                        xs: 12,
                        sm: 6,
                        md: 4,
                        lg: 4
                      }}
                    >
                      <ProductCard
                        product={
                          product
                        }
                      />
                    </Grid>
                  )
                )}
              </Grid>


              {/* Pagination */}
              {data.totalPages >
                1 && (
                <Stack
                  alignItems="center"
                  mt={5}
                >
                  <Pagination
                    count={
                      data.totalPages
                    }
                    page={
                      data.page + 1
                    }
                    onChange={
                      handlePageChange
                    }
                    color="primary"
                    size={
                      isMobile
                        ? "small"
                        : "medium"
                    }
                    showFirstButton
                    showLastButton
                  />
                </Stack>
              )}
            </>
          )}
        </Grid>
      </Grid>


      {/* Mobile filters */}
      <Drawer
        anchor="left"
        open={mobileFilters}
        onClose={() =>
          setMobileFilters(false)
        }
      >
        <Box
          sx={{
            width: {
              xs: "85vw",
              sm: 350
            },
            pt: 1
          }}
        >
          {filterContent}
        </Box>
      </Drawer>
    </Container>
  );
}
